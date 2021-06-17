package com.flowapp.GasLine.Models;

import java.util.List;

public class GasPipeResult {

    private final List<GasPipe> beforeLines;
    private final List<GasPipe> loops;
    private final List<GasPipe> afterLines;
    private final List<GasPipe> complementaryLines;
    private final PanhandlePResult panhandlePResult;
    private final String steps;


    public GasPipeResult(List<GasPipe> beforeLines, List<GasPipe> loops, List<GasPipe> afterLines,List<GasPipe> complementaryLines, PanhandlePResult panhandlePResult, String steps) {
        this.beforeLines = beforeLines;
        this.loops = loops;
        this.afterLines = afterLines;
        this.complementaryLines = complementaryLines;
        this.panhandlePResult = panhandlePResult;
        this.steps = steps;
    }

    public List<GasPipe> getBeforeLines() {
        return beforeLines;
    }

    public List<GasPipe> getLoops() {
        return loops;
    }

    public List<GasPipe> getAfterLines() {
        return afterLines;
    }

    public List<GasPipe> getComplementaryLines() {
        return complementaryLines;
    }

    public PanhandlePResult getPanhandlePResult() {
        return panhandlePResult;
    }

    public String getSteps() {
        return steps;
    }
}
