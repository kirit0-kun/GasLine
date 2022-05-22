package com.flowapp.GasLine;

import com.flowapp.GasLine.Models.GasPipe;
import com.flowapp.GasLine.Models.GasPipeResult;
import com.flowapp.GasLine.Models.PanhandlePResult;
import com.flowapp.GasLine.Models.Tuple2;
import com.flowapp.GasLine.Utils.Constants;
import com.flowapp.GasLine.Utils.FileUtils;
import com.flowapp.GasLine.Utils.TableList;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GasLine {

    private StringBuilder steps;

    public GasPipeResult gasLine(
            Float iDmm,
            float loopIDmm,
            float totalLengthKm,
            float flowRateScfHr,
            float increasedFlowRateScfHr,
            float consumerFlowRateScfHr,
            Float p2,
            Float p1,
            Float tAvg,
            float maxPressure,
            float roughness,
            Float c1y,
            Float c2y,
            Float c3y,
            Float c4y,
            Float nitrogenY, Float h2sY) {

        clear();

        final float totalLengthMiles = totalLengthKm / Constants.KmInMile;
        println("L = {} / {} = {} Miles", totalLengthKm, Constants.KmInMile, totalLengthMiles);
        final float flowRateScfDay = flowRateScfHr * Constants.HoursInDay;
        println("Q = {} * {} = {} SCF/hr", flowRateScfHr, Constants.HoursInDay, flowRateScfDay);
        final float increasedFlowRateScfDay = increasedFlowRateScfHr * Constants.HoursInDay;
        final float consumerFlowRateScfDay = consumerFlowRateScfHr * Constants.HoursInDay;

        final var oldTAvg = tAvg;
        tAvg = tAvg + Constants.RankinZeroF;
        println("Tavg = {} + {} = {}", oldTAvg, Constants.RankinZeroF, tAvg);

        final float ymSum = calculateCompsTable(c1y,c2y,c3y,c4y,nitrogenY, h2sY);
        println("ΣYiMi = {}", ymSum);
        final float spGr = ymSum / Constants.AirMolecularWeight;
        println("δg = ΣYiMi / {} = {}/{} = {}", Constants.AirMolecularWeight, ymSum, Constants.AirMolecularWeight, spGr);
        PanhandlePResult panhandlePResult = null;
        if (p1 == null) {
            panhandlePResult = calculateP1(p2, spGr, iDmm, flowRateScfDay, totalLengthMiles, tAvg, maxPressure, true);
            p1 = panhandlePResult.getP1();
            println("P1 = {} Psi", p1);
        } else if (p2 == null) {
            if (p1 > maxPressure) {
                println("P1 > Max Pressure, which is not feasible, therefore taking P1 = MaxPressure");
                p1 = maxPressure;
            }
            panhandlePResult = calculateP2(p1, spGr, iDmm, flowRateScfDay, totalLengthMiles, tAvg, true);
            p2 = panhandlePResult.getP2();
            println("P2 = {} Psi", p2);
        }
        final List<GasPipe> beforeLines = new ArrayList<>();
        if (p1 > maxPressure) {
            println("since P1 > Max Pressure");
            println("then more than one station is required");
            p1 = maxPressure;
            beforeLines.addAll(calculateBoosterStations(iDmm, p2, tAvg, maxPressure, totalLengthMiles, flowRateScfDay, spGr, panhandlePResult.getZ(), 0));
        } else {
            println("since P1 < Max Pressure");
            println("then one station is sufficient");
            final GasPipe line = new GasPipe(p1, p2, flowRateScfDay, iDmm, 0, totalLengthMiles);
            beforeLines.add(line);
        }

        println("If Q is increased to {} SCf/Day", increasedFlowRateScfDay);
        println("Using loop with ID = {} mm", loopIDmm);
        final List<GasPipe> loops = new ArrayList<>();
        final List<GasPipe> linesToLoop = new ArrayList<>();
        final List<GasPipe> complementaryLines = new ArrayList<>();
        if (beforeLines.size() > 1) {
            linesToLoop.add(beforeLines.get(0));
        }
        linesToLoop.add(beforeLines.get(beforeLines.size() - 1));
        Collections.reverse(linesToLoop);
        for (int i = linesToLoop.size() - 1; i >= 0; i--) {
            final var lineToLoop = linesToLoop.get(i);
            if (linesToLoop.size() > 1) {
                println("For station #{}", beforeLines.size()-i);
            }
            final var loopy = calculateLoopFraction(roughness, iDmm, loopIDmm, flowRateScfDay, increasedFlowRateScfDay);
            if (loopy > 1) {
                println("Since y > 1, then an increased flow rate using a loop is not feasible");
                break;
            }
            final var loopx = 1 - loopy;
            final float loopLength = lineToLoop.getLengthMile() * loopy;
            final float loopStart = lineToLoop.getStartMile() + lineToLoop.getLengthMile() * loopx;
            final float loopP1 = calculateP2(lineToLoop.getP1(), spGr, lineToLoop.getIDmm(), increasedFlowRateScfDay, lineToLoop.getLengthMile() - loopLength, tAvg, false).getP2();
            println("Then loop length (yl) = {} Miles", loopLength);
            println("loop position = {} Miles from the first point", loopStart);
            println("Loop head (P1) = {} Psi", loopP1);
            final GasPipe loop = new GasPipe(loopP1, lineToLoop.getP2(), increasedFlowRateScfDay, loopIDmm, loopStart, loopLength);
            loops.add(loop);
            if (linesToLoop.size() > 1 && (beforeLines.size()-i) > 1) {
                final GasPipe complementaryLine = new GasPipe(lineToLoop.getP1(), loopP1, increasedFlowRateScfDay, lineToLoop.getIDmm(), lineToLoop.getStartMile(), lineToLoop.getLengthMile() - loopLength);
                complementaryLines.add(complementaryLine);
            }
        }

        println("Redesigning using booster stations:-");
        final List<GasPipe> redesignedAfterLines = new ArrayList<>(calculateBoosterStations(iDmm, p2, tAvg, p1,
                totalLengthMiles, increasedFlowRateScfDay, spGr,
                Objects.requireNonNull(panhandlePResult).getZ(), 0));

        println("Using booster stations:-");
        final GasPipe firstLine = beforeLines.get(0);
        println("For first station:-");
        final List<GasPipe> firstAfterLinesSeries = new ArrayList<>(calculateBoosterStations(iDmm, p2, tAvg, p1,
                firstLine.getLengthMile(), increasedFlowRateScfDay, spGr,
                Objects.requireNonNull(panhandlePResult).getZ(), 0));
        final List<GasPipe> afterLines = new ArrayList<>(firstAfterLinesSeries);
        for (int i = 1; i < beforeLines.size() - 1; i++) {
            final GasPipe beforeLine = beforeLines.get(i);
            final List<GasPipe> after = afterLines.stream().map(line -> new GasPipe(line.getP1(),
                    line.getP2(), line.getFlowRateScfDay(), line.getIDmm(),
                    line.getStartMile() + beforeLine.getStartMile(),
                    line.getLengthMile())).toList();
            afterLines.addAll(after);
        }
        if (beforeLines.size() > 1) {
            final GasPipe lastLine = beforeLines.get(beforeLines.size() - 1);
            println("For last station:-");
            afterLines.addAll(calculateBoosterStations(iDmm, p2, tAvg, p1,
                    lastLine.getLengthMile(), increasedFlowRateScfDay, spGr,
                    Objects.requireNonNull(panhandlePResult).getZ(), lastLine.getStartMile()));
        }

        println("Drawing");
        println("Before Increase");
        for (int i = 0; i < beforeLines.size(); i++) {
            final var line = beforeLines.get(i);
            println("Station {}", i+1);
            renderLine(line);
        }
        println("Loops");
        for (int i = 0; i < loops.size(); i++) {
            final var line = loops.get(i);
            println("Loop {}", i+1);
            renderLine(line);
        }
        println("After increase redesigned");
        for (int i = 0; i < redesignedAfterLines.size(); i++) {
            final var line = redesignedAfterLines.get(i);
            println("Station {}", i+1);
            renderLine(line);
        }
        println("After increase");
        for (int i = 0; i < afterLines.size(); i++) {
            final var line = afterLines.get(i);
            println("Station {}", i+1);
            renderLine(line);
        }

        println("Packed Line:-");
        float storedV = 0;
        for (int i = linesToLoop.size()-1; i>=0; i--) {
            if (beforeLines.size()>1) {
                println("For Station: {}", i+1);
            }
            final var line = linesToLoop.get(i);
            final float thisStoredV = calculateStoredVolume(line.getP1(), line.getP2(), iDmm, spGr, consumerFlowRateScfDay, line.getLengthMile(), tAvg);
            if (i != linesToLoop.size()-1) {
                storedV += thisStoredV * (beforeLines.size() - 1);
            } else {
                storedV += thisStoredV;
            }
        }
        if (beforeLines.size()>1) {
            println("Total Vstored = {} SCF", storedV);
        }
        final float avgQ = (consumerFlowRateScfDay + flowRateScfDay) / 2;
        println("Qavg = {} Scf/Day", avgQ);
        final float strQ = Math.abs(flowRateScfDay - consumerFlowRateScfDay);
        println("Qstr = {} Scf/Day", strQ);
        final float avgTime = storedV / avgQ;
        println("Tavg = {}/{} = {} days", storedV, avgQ, avgTime);
        final float strTime = storedV / strQ;
        println("Tstr = {}/{} = {} days", storedV, strQ, strTime);
        println("Then t (worst condition) = {} days", Math.min(strTime, avgTime));

        return new GasPipeResult(beforeLines, loops, redesignedAfterLines, afterLines, complementaryLines, panhandlePResult, steps.toString());
    }

    private void renderLine(GasPipe line) {

        final List<Object> xs = new ArrayList<>();
        xs.add("X");
        final List<Object> ls = new ArrayList<>();
        ls.add("L, mile");
        final List<Object> fromStartLs = new ArrayList<>();
        fromStartLs.add("L from start, mile");
        final List<Object> pxs = new ArrayList<>();
        pxs.add("Px, psi");

        for (float x = 0; x <= 1; x += 0.2f) {
            xs.add(x);
            final float l = line.getLengthMile() * x;
            ls.add(l);
            if (line.getStartMile() > 0) {
                final float lFromStart = l + line.getStartMile();
                fromStartLs.add(lFromStart);
            }
            pxs.add(line.calculatePx(x));
        }

        final List<Object[]> steps = new ArrayList<>();
        steps.add(xs.toArray(new Object[0]));
        steps.add(ls.toArray(new Object[0]));
        if (fromStartLs.size() > 1) {
            steps.add(fromStartLs.toArray(new Object[0]));
        }
        steps.add(pxs.toArray(new Object[0]));
        println("P1 = {} Psi, P2 = {} Psi, L = {} Mile", line.getP1(), line.getP2(), line.getLengthMile());
        renderTable(steps);
    }

    private List<GasPipe> calculateBoosterStations(Float iDmm, Float p2, Float tAvg, float p1, float totalLengthMiles, float flowRateScfDay, float spGr, float z, float offset) {
        final List<GasPipe> beforeLines = new ArrayList<>();
        final float stationLength = calculateStationLengthMile(p1, p2, flowRateScfDay, iDmm, spGr, tAvg, z);
        final int numberOfStations = (int) Math.ceil(totalLengthMiles /stationLength);
        println("calculate length for (P1 = {} Psi) = {} Miles, therefore requires {} stations", p1, stationLength, numberOfStations);
        float remainingLength = totalLengthMiles;
        for (int i = 1; i <= numberOfStations; i++) {
            final float start = offset + stationLength * (i-1);
            final float length = Math.min(stationLength, remainingLength);
            float stationP1;
            if (length == stationLength) {
                stationP1 = p1;
            } else {
                println("Calculating P1 for length = {} Miles", length);
                stationP1 = calculateP1(p2, spGr, iDmm, flowRateScfDay, length, tAvg, p1, true).getP1();
                println("Then P1 = {} Psi for length {} Miles", stationP1, length);
            }
            final GasPipe line = new GasPipe(stationP1, p2, flowRateScfDay, iDmm, start, length);
            beforeLines.add(line);
            remainingLength -= length;
        }
        println("Required stations:-");
        renderBeforeLines(beforeLines);
        return beforeLines;
    }

    private void renderBeforeLines(List<GasPipe> beforeLines) {
        final List<Object[]> steps = new ArrayList<>();
        steps.add(new Object[]{ "Station", "P1, Psi", "P2, Psi", "Length, Miles"});
        for (int i = 0; i < beforeLines.size(); i++) {
            final var station = beforeLines.get(i);
            steps.add(new Object[]{ i+1, station.getP1(), station.getP2(), station.getLengthMile()});
        }
        renderTable(steps);
    }

    private float calculateStoredVolume(float p1,
                                               float p2,
                                               float iDmm,
                                               float spGr,
                                               float qAvgScfD,
                                               float lengthMile,
                                               float tAvg) {
        
        final float iDm = iDmm / Constants.MmInMeter; //iDm
        final float temp = 28.8f * 520 / 14.7f;

        println("At normal conditions:-");
        final float pAvg = (float) ((2/3.0f) * (p1 + (Math.pow(p2, 2) / (p1 + p2))));
        println("Pavg = {}", pAvg);
        final float vn = (float) (temp* Math.pow(39.37f * iDm, 2) * lengthMile * pAvg / tAvg);
        println("Vn = {} SCF", vn);

        println("At packed conditions:-");
        final var packedP2 = calculateP2(p1, spGr, iDmm, qAvgScfD, lengthMile, tAvg, true).getP2();
        println("P2 packed = {} Psi", packedP2);
        final float packedAvgP = calculateAvgPressure(p1, packedP2);
        println("Pavg packed = {} Psi", packedAvgP);
        final float packedV = (float) (temp * Math.pow(39.37f * iDm, 2) * packedAvgP * lengthMile / tAvg);
        println("Vp = {} SCF", packedV);

        final float vStore = packedV - vn; // vStore
        println("Vstore = {} SCF", vStore);
        return vStore;
    }

    private float calculateLoopFraction(float roughness,
                                               float lineDmm,
                                               float loopDmm,
                                               float qBeforeScfDay,
                                               float qAfterScfDay) {

        final var lineDm = lineDmm / Constants.MmInMeter;
        final var loopDm = loopDmm / Constants.MmInMeter;

        final float eDLine = 0.001f * roughness / lineDm;
        final float fLine = (float) Math.pow(1.14f - 2 * Math.log10(eDLine), -2);

        final float eDLoop = 0.001f * roughness / loopDm;
        final float fLoop = (float) Math.pow(1.14f - 2 * Math.log10(eDLoop), -2);

        final float w = (float) (Math.pow(loopDm/lineDm, 2.5f)*Math.pow(fLine/fLoop, 0.5f));

        final float y = (float) ((1-Math.pow(qBeforeScfDay/qAfterScfDay, 2)) / (1-1/Math.pow(1+w, 2)));

        final List<Object[]> steps = new ArrayList<>();
        steps.add(new Object[]{ "", "Line", "Loop", ""});
        steps.add(new Object[]{ "I.D", lineDm, loopDm, "w = " + w});
        steps.add(new Object[]{ "e/d", String.format("%.6f", eDLine), String.format("%.6f", eDLoop), "y = " + String.format("%.4f",y)});
        steps.add(new Object[]{ "F", String.format("%.6f", fLine), String.format("%.6f",fLoop), ""});
        renderTable(steps);
        return y;
    }

    private float calculateStationLengthMile(float p1,
                                                    float p2,
                                                    float qScfDay,
                                                    float iDmm,
                                                    float spGr,
                                                    float tAvg,
                                                    float z) {
        return (float) ((Math.pow(p1, 2) - Math.pow(p2, 2)) / (tAvg*z*Math.pow(spGr,0.961f)) * (Math.pow((737 * (Math.pow(iDmm/Constants.MmInInch, 2.53f)) * Math.pow(520, 1.02f)) / (qScfDay*Math.pow(14.7f, 1.02f)), 1/0.51f)));
    }

    private PanhandlePResult calculateP1(float p2,
                                                float spGr,
                                                float idMM,
                                                float qScfD,
                                                float lengthMile,
                                                float tAvg, float maxPressure, boolean print) {

        final float pc = 709.604f - 58.718f * spGr; // Pc
        final float tc = 170.492f + 307.344f * spGr; // Tc
        final float tr = tAvg / tc; // TR
        if (print) {
            println("Pc = {} Psi, Tc = {} F, TR = {}", pc, tc, tr);
        }
        final List<Object[]> attempts = new ArrayList<>();
        attempts.add(new Object[]{ "Pass, Psi", "Pavg, Psi", "Pr", "z", "P1(calc), Psi"});
        float p1As = maxPressure; // p1As
        float pAvg; // pAvg
        float pr; // PR
        float z; //z

        // Loop
        while (true) {
            pAvg = calculateAvgPressure(p1As, p2);
            pr = pAvg / pc;
            z = 1 + 0.257f * pr - 0.533f * pr / tr;
            final float p1Calc = (float) Math.pow(Math.pow(p2, 2) + Math.pow(qScfD / (27998.0133f * Math.pow(idMM / Constants.MmInInch, 2.53f)), 1.9608f) * Math.pow(spGr, 0.961f) * tAvg * z * lengthMile, 0.5f); // p1Calc
            final var temp = Math.abs(p1Calc - p1As);

            attempts.add(new Object[]{ p1As, pAvg, pr, z, p1Calc });
            p1As = p1Calc;
            if (temp <= 0.9) {
                break;
            }
        }
        if (print) {
            renderTable(attempts);
        }
        return new PanhandlePResult(p1As, p2, pAvg, pr, z, pc, tc, tr);
    }
    
    private PanhandlePResult calculateP2(float p1,
                                                float spGr,
                                                float idMM,
                                                float qScfD,
                                                float lengthMile,
                                                float tAvg, boolean print ) {

        final float pc = 709.604f - 58.718f * spGr; // Pc
        final float tc = 170.492f + 307.344f * spGr; // Tc
        final float tr = tAvg / tc; // TR
        if (print) {
            println("Pc = {} Psi, Tc = {} F, TR = {}", pc, tc, tr);
        }
        final List<Object[]> attempts = new ArrayList<>();
        attempts.add(new Object[]{ "Pass, Psi", "Pavg, Psi", "Pr", "z", "P2(calc), Psi"});
        float p2As = p1/3;
        float pAvg;
        float pr;
        float z;

        while (true) {
            pAvg = calculateAvgPressure(p1, p2As);
            pr = pAvg / pc;
            z = 1 + 0.257f * pr - 0.533f * pr / tr;
            final float p2Calc = (float) Math.pow(Math.pow(p1, 2) - Math.pow(qScfD / (27998.0134f * Math.pow(idMM / Constants.MmInInch, 2.53f)), 1.9608f) * Math.pow(spGr, 0.961f) * tAvg * z * lengthMile, 0.5f); // p1Calc
            final var temp = Math.abs(p2Calc - p2As);
            attempts.add(new Object[]{ p2As, pAvg, pr, z, p2Calc });
            p2As = p2Calc;
            if (temp <= 0.9) {
                break;
            }
        }
        if (print) {
            renderTable(attempts);
        }
        return new PanhandlePResult(p1, p2As, pAvg, pr, z, pc, tc, tr);
    }

    private float calculateAvgPressure(float p1, float p2) {
        return (float) (2 * ((Math.pow(p1, 3) - Math.pow(p2, 3)) / (Math.pow(p1, 2) - Math.pow(p2, 2))) / 3);
    }

    private float calculateCompsTable(
            Float c1y,
            Float c2y,
            Float c3y,
            Float c4y,
            Float nitrogenY,
            Float h2sY) {
        float sum = 0;
        final List<Object[]> lines = new ArrayList<>();
        lines.add(new Object[]{ "Comp.", "yi", "Mi", "yi.Mi" });
        final var comps = List.of(
                Tuple2.of("C1", Tuple2.of(c1y, Constants.C1MolecularWeight)),
                Tuple2.of("C2", Tuple2.of(c2y, Constants.C2MolecularWeight)),
                Tuple2.of("C3", Tuple2.of(c3y, Constants.C3MolecularWeight)),
                Tuple2.of("C4", Tuple2.of(c4y, Constants.C4MolecularWeight)),
                Tuple2.of("N2", Tuple2.of(nitrogenY, Constants.NitrogenMolecularWeight)),
                Tuple2.of("H2S", Tuple2.of(h2sY, Constants.H2SMolecularWeight))
        );
        for (var comp: comps) {
            if (comp.getSecond().getFirst() != null && comp.getSecond().getFirst() > 0) {
                final float ym = comp.getSecond().getSecond() * comp.getSecond().getFirst();
                sum += ym;
                lines.add(new Object[]{ comp.getFirst(), comp.getSecond().getFirst(), comp.getSecond().getSecond(), ym});
            }
        }
        renderTable(lines);
        return sum;
    }

    private void renderTable(List<Object[]> args) {
        renderTable(args.toArray(new Object[0][0]));
    }

    private void renderTable(Object[] ... args) {
        final var temp = args[0];
        final String[] firstRow = new String[temp.length];
        for (int i = 0; i < temp.length; i++) {
            firstRow[i] = temp[i].toString();
        }
        TableList at = new TableList(firstRow).withUnicode(true);
        final var newRows = Arrays.stream(args).skip(1).map(row -> {
            final String[] newRow = new String[row.length];
            for (int i = 0; i < row.length; i++) {
                final Object object = row[i];
                if (object instanceof Number) {
                    newRow[i] = formatNumber((Number) object);
                } else {
                    newRow[i] = object.toString();
                }
            }
            return newRow;
        }).collect(Collectors.toList());
        for (var row: newRows) {
            at.addRow(row);
        }
        String rend = at.render();
        println(rend);
    }

    private void println(@NotNull String pattern, Object... args) {
        final String message = format(pattern, args);
        steps.append(message).append('\n');
        FileUtils.printOut(message);
    }

    private void clear() {
        steps = new StringBuilder();
        FileUtils.clear();
    }

    private String formatNumber(Number number) {
        final var value = number.floatValue();
        if (value < 0) {
            return String.format("%.7f", value);
        } else if (value == 0) {
            return  "0";
        } else {
            return String.format("%.4f", value).replace(".0000", "");
        }
    }

    @NotNull
    private String format(@NotNull String pattern, Object... args) {
        Pattern rePattern = Pattern.compile("\\{([0-9+-]*)}", Pattern.CASE_INSENSITIVE);
        Matcher matcher = rePattern.matcher(pattern);
        int counter = -1;
        while (matcher.find()) {
            counter++;
            String number = matcher.group(1);
            if (number == null) {
                number = "";
            }
            if (!number.isBlank()) {
                if (number.equals("+")) {
                    number = "\\+";
                    counter++;
                } else if (number.equals("-")) {
                    counter--;
                } else {
                    counter = Integer.parseInt(number);
                }
            }
            counter = clamp(counter, 0, args.length - 1);
            String toChange = "\\{" + number + "}";
            Object object = args[counter];
            String objectString;
            if (object instanceof Number) {
                objectString = formatNumber((Number) object);
            } else {
                objectString = object.toString();
            }
            String result = objectString;
            pattern = pattern.replaceFirst(toChange, result);
        }
        return pattern;
    }

    private <T extends Comparable<T>> T clamp(T val, T min, T max) {
        if (val.compareTo(min) < 0) return min;
        else if (val.compareTo(max) > 0) return max;
        else return val;
    }
}
