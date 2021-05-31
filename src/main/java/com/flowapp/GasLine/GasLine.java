package com.flowapp.GasLine;

import com.flowapp.GasLine.Models.PanhandlePResult;
import com.flowapp.GasLine.Models.Tuple2;
import com.flowapp.GasLine.Utils.Constants;
import com.flowapp.GasLine.Utils.FileUtils;
import com.flowapp.GasLine.Utils.TableList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GasLine {

    private static StringBuilder steps;

    public static void main(String[] args) {

        clear();

        final Float iDmm = 385.8f;
        final float totalLengthKm = 120;
        final float flowRateScfHr = 7.5f * 1_000_000f;
        Float p2 = null;
        Float p1 = 960f;
        final Float tMax = 80f;
        final Float tMin = 60f;
        Float tAvg = null;
        final float maxPressure = 1000f;
        final float roughness = 0.061f;

        final Float c1y = 0.88f;
        final Float c2y = 0.1f;
        final Float c3y = null;
        final Float c4y = null;
        final Float nitrogenY = 0.02f;

        final float totalLengthMiles = totalLengthKm / Constants.KmInMile;
        final float flowRateScfDay = flowRateScfHr * Constants.HoursInDay;

        if (tAvg == null) {
            tAvg = (tMax + tMin) / 2 + Constants.RankinZeroF;
            println("Tavg = ({} + {}) / 2 + {} = {}", tMax, tMin, Constants.RankinZeroF, tAvg);
        } else {
            final var oldTAvg = tAvg;
            tAvg = tAvg + Constants.RankinZeroF;
            println("Tavg = {} + {} = {}", oldTAvg, Constants.RankinZeroF, tAvg);
        }

        final float ymSum = calculateCompsTable(c1y,c2y,c3y,c4y,nitrogenY);
        println("ΣYiMi = {}", ymSum);
        final float spGr = ymSum / Constants.AirMolecularWeight;
        println("δg = ΣYiMi / {} = {}/{} = {}", Constants.AirMolecularWeight, ymSum, Constants.AirMolecularWeight, spGr);
        PanhandlePResult panhandlePResult = null;
        if (p1 == null) {
            panhandlePResult = calculateP1(p2, spGr, iDmm, flowRateScfDay, totalLengthMiles, tAvg, maxPressure);
            p1 = panhandlePResult.getP1();
            println("P1 = {} Psi", p1);
        } else if (p2 == null) {
            panhandlePResult = calculateP2(p1, spGr, iDmm, flowRateScfDay, totalLengthMiles, tAvg);
            p2 = panhandlePResult.getP2();
            println("P2 = {} Psi", p2);
        }

        final float stationLength = calculateStationLengthMile(p1, p2, flowRateScfDay * 1.25f, iDmm, spGr, tAvg, panhandlePResult.getZ());
        println("{}", stationLength);
        final var loopy = calculateLoopFraction(roughness, iDmm, iDmm, flowRateScfDay, flowRateScfDay * 1.35f);
        println("{}", loopy);
        final float px = calculatePx(p1, p2, 0.5f);
        println("{}", px);
        final float x = calculateX(p1, p2, px);
        println("{}", x);
        final float storedV = calculateStoredVolume(p1, p2, iDmm, spGr, flowRateScfDay * 1.3333f/2, totalLengthMiles, tAvg);
        println("{}", storedV);
        System.out.println(steps);
    }

    private static float calculateStoredVolume(float p1,
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
        final var packedP2 = calculateP2(p1, spGr, iDmm, qAvgScfD, lengthMile, tAvg).getP2();
        println("P2 packed = {} Psi", packedP2);
        final float packedAvgP = calculateAvgPressure(p1, packedP2);
        println("Pavg packed = {} Psi", packedAvgP);
        final float packedV = (float) (temp * Math.pow(39.37f * iDm, 2) * packedAvgP * lengthMile / tAvg);
        println("Vp = {} SCF", packedV);

        final float vStore = packedV - vn; // vStore
        println("Vstore = {} SCF", vStore);
        return vStore;
    }

    private static float calculatePx(float p1,
                                    float p2,
                                    float x) {

        final float px = (float) Math.pow(Math.pow(p1,2) - ((Math.pow(p1,2) - Math.pow(p2, 2)) * x), 0.5); // Px
        return px;
    }

    private static float calculateX(float p1,
                                    float p2,
                                    float px) {

        final float x = (float) ((Math.pow(p1,2) - Math.pow(px, 2))/(Math.pow(p1,2) - Math.pow(p2, 2)));
        return x;
    }

    private static float calculateLoopFraction(float roughness,
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

    private static float calculateStationLengthMile(float p1,
                                                    float p2,
                                                    float qScfDay,
                                                    float iDmm,
                                                    float spGr,
                                                    float tAvg,
                                                    float z) {
        return (float) ((Math.pow(p1, 2) - Math.pow(p2, 2)) / (tAvg*z*Math.pow(spGr,0.961f)) * (Math.pow((737 * (Math.pow(iDmm/Constants.MmInInch, 2.53f)) * Math.pow(520, 1.02f)) / (qScfDay*Math.pow(14.7f, 1.02f)), 1/0.51f)));
    }

    private static PanhandlePResult calculateP1(float p2,
                                                float spGr,
                                                float idMM,
                                                float qScfD,
                                                float lengthMile,
                                                float tAvg, float maxPressure ) {

        final float pc = 709.604f - 58.718f * spGr; // Pc
        final float tc = 170.492f + 307.344f * spGr; // Tc
        final float tr = tAvg / tc; // TR

        println("Pc = {} Psi, Tc = {} F, TR = {}", pc, tc, tr);
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
        renderTable(attempts);
        return new PanhandlePResult(p1As, p2, pAvg, pr, z, pc, tc, tr);
    }
    
    private static PanhandlePResult calculateP2(float p1,
                                                float spGr,
                                                float idMM,
                                                float qScfD,
                                                float lengthMile,
                                                float tAvg ) {

        final float pc = 709.604f - 58.718f * spGr; // Pc
        final float tc = 170.492f + 307.344f * spGr; // Tc
        final float tr = tAvg / tc; // TR

        println("Pc = {} Psi, Tc = {} F, TR = {}", pc, tc, tr);
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

        renderTable(attempts);
        return new PanhandlePResult(p1, p2As, pAvg, pr, z, pc, tc, tr);
    }

    private static float calculateAvgPressure(float p1, float p2) {
        return (float) (2 * ((Math.pow(p1, 3) - Math.pow(p2, 3)) / (Math.pow(p1, 2) - Math.pow(p2, 2))) / 3);
    }

    private static float calculateCompsTable(
            Float c1y,
            Float c2y,
            Float c3y,
            Float c4y,
            Float nitrogenY
    ) {
        float sum = 0;
        final List<Object[]> lines = new ArrayList<>();
        lines.add(new Object[]{ "Comp.", "yi", "Mi", "yi.Mi" });
        final var comps = List.of(
                Tuple2.of("C1", Tuple2.of(c1y, Constants.C1MolecularWeight)),
                Tuple2.of("C2", Tuple2.of(c2y, Constants.C2MolecularWeight)),
                Tuple2.of("C3", Tuple2.of(c3y, Constants.C3MolecularWeight)),
                Tuple2.of("C4", Tuple2.of(c4y, Constants.C4MolecularWeight)),
                Tuple2.of("N2", Tuple2.of(nitrogenY, Constants.NitrogenMolecularWeight))
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

    private static void renderTable(List<Object[]> args) {
        renderTable(args.toArray(new Object[0][0]));
    }

    private static void renderTable(Object[] ... args) {
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

    private static void println(@NotNull String pattern, Object... args) {
        final String message = format(pattern, args);
        steps.append(message).append('\n');
        FileUtils.printOut(message);
    }

    private static void clear() {
        steps = new StringBuilder();
        FileUtils.clear();
    }

    private static String formatNumber(Number number) {
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
    private static String format(@NotNull String pattern, Object... args) {
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

    private static <T extends Comparable<T>> T clamp(T val, T min, T max) {
        if (val.compareTo(min) < 0) return min;
        else if (val.compareTo(max) > 0) return max;
        else return val;
    }
}
