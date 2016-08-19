/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.benchmark.impl.result;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.optaplanner.benchmark.impl.report.BenchmarkReport;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.SolverConfigContext;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.config.util.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the benchmarks on multiple {@link Solver} configurations on multiple problem instances (data sets).
 */
@XStreamAlias("plannerBenchmarkResult")
public class PlannerBenchmarkResult {

    private String name;
    private Boolean aggregation;
    @XStreamOmitField // Moving or renaming a report directory after creation is allowed
    private File benchmarkReportDirectory;

    // If it is an aggregation, many properties can stay null

    private Integer availableProcessors = null;
    private String loggingLevelOptaPlannerCore = null;
    private String loggingLevelDroolsCore = null;
    private Long maxMemory = null;
    private String optaPlannerVersion = null;
    private String javaVersion = null;
    private String javaVM = null;
    private String operatingSystem = null;

    private Integer parallelBenchmarkCount = null;
    private Long warmUpTimeMillisSpentLimit = null;
    private EnvironmentMode environmentMode = null;

    @XStreamImplicit(itemFieldName = "solverBenchmarkResult")
    private List<SolverBenchmarkResult> solverBenchmarkResultList = null;
    @XStreamImplicit(itemFieldName = "unifiedProblemBenchmarkResult")
    private List<ProblemBenchmarkResult> unifiedProblemBenchmarkResultList = null;

    private Date startingTimestamp = null;
    private Long benchmarkTimeMillisSpent = null;

    // ************************************************************************
    // Report accumulates
    // ************************************************************************

    private Integer failureCount = null;
    private Long averageProblemScale = null;
    private Score averageScore = null;
    private SolverBenchmarkResult favoriteSolverBenchmarkResult = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getAggregation() {
        return aggregation;
    }

    public void setAggregation(Boolean aggregation) {
        this.aggregation = aggregation;
    }

    public File getBenchmarkReportDirectory() {
        return benchmarkReportDirectory;
    }

    public void setBenchmarkReportDirectory(File benchmarkReportDirectory) {
        this.benchmarkReportDirectory = benchmarkReportDirectory;
    }

    public Integer getAvailableProcessors() {
        return availableProcessors;
    }

    public String getLoggingLevelOptaPlannerCore() {
        return loggingLevelOptaPlannerCore;
    }

    public String getLoggingLevelDroolsCore() {
        return loggingLevelDroolsCore;
    }

    public Long getMaxMemory() {
        return maxMemory;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public String getJavaVM() {
        return javaVM;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public String getOptaPlannerVersion() {
        return optaPlannerVersion;
    }

    public Integer getParallelBenchmarkCount() {
        return parallelBenchmarkCount;
    }

    public void setParallelBenchmarkCount(Integer parallelBenchmarkCount) {
        this.parallelBenchmarkCount = parallelBenchmarkCount;
    }

    public Long getWarmUpTimeMillisSpentLimit() {
        return warmUpTimeMillisSpentLimit;
    }

    public void setWarmUpTimeMillisSpentLimit(Long warmUpTimeMillisSpentLimit) {
        this.warmUpTimeMillisSpentLimit = warmUpTimeMillisSpentLimit;
    }

    public EnvironmentMode getEnvironmentMode() {
        return environmentMode;
    }

    public List<SolverBenchmarkResult> getSolverBenchmarkResultList() {
        return solverBenchmarkResultList;
    }

    public void setSolverBenchmarkResultList(List<SolverBenchmarkResult> solverBenchmarkResultList) {
        this.solverBenchmarkResultList = solverBenchmarkResultList;
    }

    public List<ProblemBenchmarkResult> getUnifiedProblemBenchmarkResultList() {
        return unifiedProblemBenchmarkResultList;
    }

    public void setUnifiedProblemBenchmarkResultList(List<ProblemBenchmarkResult> unifiedProblemBenchmarkResultList) {
        this.unifiedProblemBenchmarkResultList = unifiedProblemBenchmarkResultList;
    }

    public Date getStartingTimestamp() {
        return startingTimestamp;
    }

    public void setStartingTimestamp(Date startingTimestamp) {
        this.startingTimestamp = startingTimestamp;
    }

    public Long getBenchmarkTimeMillisSpent() {
        return benchmarkTimeMillisSpent;
    }

    public void setBenchmarkTimeMillisSpent(Long benchmarkTimeMillisSpent) {
        this.benchmarkTimeMillisSpent = benchmarkTimeMillisSpent;
    }

    public Integer getFailureCount() {
        return failureCount;
    }

    public Long getAverageProblemScale() {
        return averageProblemScale;
    }

    public Score getAverageScore() {
        return averageScore;
    }

    public SolverBenchmarkResult getFavoriteSolverBenchmarkResult() {
        return favoriteSolverBenchmarkResult;
    }

    // ************************************************************************
    // Smart getters
    // ************************************************************************

    public boolean hasMultipleParallelBenchmarks() {
        return parallelBenchmarkCount == null || parallelBenchmarkCount > 1;
    }

    public boolean hasAnyFailure() {
        return failureCount > 0;
    }

    public int getMaximumSubSingleCount() {
        int maximumSubSingleCount = 0;
        for (ProblemBenchmarkResult problemBenchmarkResult : unifiedProblemBenchmarkResultList) {
            int problemMaximumSubSingleCount = problemBenchmarkResult.getMaximumSubSingleCount();
            if (problemMaximumSubSingleCount > maximumSubSingleCount) {
                maximumSubSingleCount = problemMaximumSubSingleCount;
            }
        }
        return maximumSubSingleCount;
    }

    // ************************************************************************
    // Accumulate methods
    // ************************************************************************

    public void initBenchmarkReportDirectory(File benchmarkDirectory) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(startingTimestamp);
        if (StringUtils.isEmpty(name)) {
            name = timestamp;
        }
        if (!benchmarkDirectory.mkdirs()) {
            if (!benchmarkDirectory.isDirectory()) {
                throw new IllegalArgumentException("The benchmarkDirectory (" + benchmarkDirectory
                        + ") already exists, but is not a directory.");
            }
            if (!benchmarkDirectory.canWrite()) {
                throw new IllegalArgumentException("The benchmarkDirectory (" + benchmarkDirectory
                        + ") already exists, but is not writable.");
            }
        }
        int duplicationIndex = 0;
        do {
            String directoryName = timestamp + (duplicationIndex == 0 ? "" : "_" + duplicationIndex);
            duplicationIndex++;
            benchmarkReportDirectory = new File(benchmarkDirectory,
                    BooleanUtils.isFalse(aggregation) ? directoryName : directoryName + "_aggregation");
        } while (!benchmarkReportDirectory.mkdir());
        for (ProblemBenchmarkResult problemBenchmarkResult : unifiedProblemBenchmarkResultList) {
            problemBenchmarkResult.makeDirs();
        }
    }

    public void initSystemProperties() {
        availableProcessors = Runtime.getRuntime().availableProcessors();
        loggingLevelOptaPlannerCore = resolveLoggingLevel("org.optaplanner.core");
        loggingLevelDroolsCore = resolveLoggingLevel("org.drools.core");
        maxMemory = Runtime.getRuntime().maxMemory();
        optaPlannerVersion = SolverFactory.class.getPackage().getImplementationVersion();
        if (optaPlannerVersion == null) {
            optaPlannerVersion = "Unjarred development snapshot";
        }
        javaVersion = "Java " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")";
        javaVM = "Java " + System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.version")
                + " (" + System.getProperty("java.vm.vendor") + ")";
        operatingSystem = System.getProperty("os.name") + " " + System.getProperty("os.arch")
                + " " + System.getProperty("os.version");
    }

    private String resolveLoggingLevel(String loggerName) {
        Logger logger = LoggerFactory.getLogger(loggerName);
        if (logger.isTraceEnabled()) {
            return "trace";
        } else if (logger.isDebugEnabled()) {
            return "debug";
        } else if (logger.isInfoEnabled()) {
            return "info";
        } else if (logger.isWarnEnabled()) {
            return "warn";
        } else if (logger.isErrorEnabled()) {
            return "error";
        } else {
            throw new IllegalStateException("Logging level for loggerName (" + loggerName + ") cannot be determined.");
        }
    }

    public int getTotalSubSingleCount() {
        int totalSubSingleCount = 0;
        for (ProblemBenchmarkResult problemBenchmarkResult : unifiedProblemBenchmarkResultList) {
            totalSubSingleCount += problemBenchmarkResult.getTotalSubSingleCount();
        }
        return totalSubSingleCount;
    }

    public void accumulateResults(BenchmarkReport benchmarkReport) {
        for (ProblemBenchmarkResult problemBenchmarkResult : unifiedProblemBenchmarkResultList) {
            problemBenchmarkResult.accumulateResults(benchmarkReport);
        }
        for (SolverBenchmarkResult solverBenchmarkResult : solverBenchmarkResultList) {
            solverBenchmarkResult.accumulateResults(benchmarkReport);
        }
        determineTotalsAndAverages();
        determineSolverRanking(benchmarkReport);
    }

    private void determineTotalsAndAverages() {
        failureCount = 0;
        long totalProblemScale = 0L;
        int problemScaleCount = 0;
        for (ProblemBenchmarkResult problemBenchmarkResult : unifiedProblemBenchmarkResultList) {
            Long problemScale = problemBenchmarkResult.getProblemScale();
            if (problemScale != null && problemScale >= 0L) {
                totalProblemScale += problemScale;
                problemScaleCount++;
            }
            failureCount += problemBenchmarkResult.getFailureCount();
        }
        averageProblemScale = problemScaleCount == 0 ? null : totalProblemScale / (long) problemScaleCount;
        Score totalScore = null;
        int solverBenchmarkCount = 0;
        boolean firstSolverBenchmarkResult = true;
        for (SolverBenchmarkResult solverBenchmarkResult : solverBenchmarkResultList) {
            EnvironmentMode solverEnvironmentMode = solverBenchmarkResult.getEnvironmentMode();
            if (firstSolverBenchmarkResult && solverEnvironmentMode != null) {
                environmentMode = solverEnvironmentMode;
                firstSolverBenchmarkResult = false;
            } else if (!firstSolverBenchmarkResult && solverEnvironmentMode != environmentMode) {
                environmentMode = null;
            }

            Score score = solverBenchmarkResult.getAverageScore();
            if (score != null) {
                if (totalScore != null && !totalScore.isCompatibleArithmeticArgument(score)) {
                    // Mixing different use cases with different score definitions.
                    totalScore = null;
                    break;
                }
                totalScore = (totalScore == null) ? score : totalScore.add(score);
                solverBenchmarkCount++;
            }
        }
        if (totalScore != null) {
            averageScore = totalScore.divide(solverBenchmarkCount);
        }
    }

    private void determineSolverRanking(BenchmarkReport benchmarkReport) {
        List<SolverBenchmarkResult> rankableSolverBenchmarkResultList = new ArrayList<>(solverBenchmarkResultList);
        // Do not rank a SolverBenchmarkResult that has a failure
        for (Iterator<SolverBenchmarkResult> it = rankableSolverBenchmarkResultList.iterator(); it.hasNext(); ) {
            SolverBenchmarkResult solverBenchmarkResult = it.next();
            if (solverBenchmarkResult.hasAnyFailure()) {
                it.remove();
            }
        }
        List<List<SolverBenchmarkResult>> sameRankingListList = createSameRankingListList(
                benchmarkReport, rankableSolverBenchmarkResultList);
        int ranking = 0;
        for (List<SolverBenchmarkResult> sameRankingList : sameRankingListList) {
            for (SolverBenchmarkResult solverBenchmarkResult : sameRankingList) {
                solverBenchmarkResult.setRanking(ranking);
            }
            ranking += sameRankingList.size();
        }
        favoriteSolverBenchmarkResult = sameRankingListList.isEmpty() ? null
                : sameRankingListList.get(0).get(0);
    }

    private List<List<SolverBenchmarkResult>> createSameRankingListList(
            BenchmarkReport benchmarkReport, List<SolverBenchmarkResult> rankableSolverBenchmarkResultList) {
        List<List<SolverBenchmarkResult>> sameRankingListList = new ArrayList<>(
                rankableSolverBenchmarkResultList.size());
        if (benchmarkReport.getSolverRankingComparator() != null) {
            Comparator<SolverBenchmarkResult> comparator = Collections.reverseOrder(
                    benchmarkReport.getSolverRankingComparator());
            Collections.sort(rankableSolverBenchmarkResultList, comparator);
            List<SolverBenchmarkResult> sameRankingList = null;
            SolverBenchmarkResult previousSolverBenchmarkResult = null;
            for (SolverBenchmarkResult solverBenchmarkResult : rankableSolverBenchmarkResultList) {
                if (previousSolverBenchmarkResult == null
                        || comparator.compare(previousSolverBenchmarkResult, solverBenchmarkResult) != 0) {
                    // New rank
                    sameRankingList = new ArrayList<>();
                    sameRankingListList.add(sameRankingList);
                }
                sameRankingList.add(solverBenchmarkResult);
                previousSolverBenchmarkResult = solverBenchmarkResult;
            }
        } else if (benchmarkReport.getSolverRankingWeightFactory() != null) {
            SortedMap<Comparable, List<SolverBenchmarkResult>> rankedMap
                    = new TreeMap<>(Collections.reverseOrder());
            for (SolverBenchmarkResult solverBenchmarkResult : rankableSolverBenchmarkResultList) {
                Comparable rankingWeight = benchmarkReport.getSolverRankingWeightFactory()
                        .createRankingWeight(rankableSolverBenchmarkResultList, solverBenchmarkResult);
                List<SolverBenchmarkResult> sameRankingList = rankedMap.get(rankingWeight);
                if (sameRankingList == null) {
                    sameRankingList = new ArrayList<>();
                    rankedMap.put(rankingWeight, sameRankingList);
                }
                sameRankingList.add(solverBenchmarkResult);
            }
            for (Map.Entry<Comparable, List<SolverBenchmarkResult>> entry : rankedMap.entrySet()) {
                sameRankingListList.add(entry.getValue());
            }
        } else {
            throw new IllegalStateException("Ranking is impossible" +
                    " because solverRankingComparator and solverRankingWeightFactory are null.");
        }
        return sameRankingListList;
    }

    // ************************************************************************
    // Merger methods
    // ************************************************************************

    public static PlannerBenchmarkResult createMergedResult(SolverConfigContext configContext,
            List<SingleBenchmarkResult> singleBenchmarkResultList) {
        PlannerBenchmarkResult mergedResult = createMergeSingleton(singleBenchmarkResultList);
        Map<SolverBenchmarkResult, SolverBenchmarkResult> solverMergeMap
                = SolverBenchmarkResult.createMergeMap(mergedResult, singleBenchmarkResultList);
        Map<ProblemBenchmarkResult, ProblemBenchmarkResult> problemMergeMap
                = ProblemBenchmarkResult.createMergeMap(mergedResult, singleBenchmarkResultList);
        for (SingleBenchmarkResult singleBenchmarkResult : singleBenchmarkResultList) {
            SolverBenchmarkResult solverBenchmarkResult = solverMergeMap.get(
                    singleBenchmarkResult.getSolverBenchmarkResult());
            ProblemBenchmarkResult problemBenchmarkResult = problemMergeMap.get(
                    singleBenchmarkResult.getProblemBenchmarkResult());
            SingleBenchmarkResult.createMerge(configContext,
                    solverBenchmarkResult, problemBenchmarkResult, singleBenchmarkResult);
        }
        return mergedResult;
    }

    protected static PlannerBenchmarkResult createMergeSingleton(List<SingleBenchmarkResult> singleBenchmarkResultList) {
        PlannerBenchmarkResult newResult = null;
        Map<PlannerBenchmarkResult, PlannerBenchmarkResult> mergeMap
                = new IdentityHashMap<>();
        for (SingleBenchmarkResult singleBenchmarkResult : singleBenchmarkResultList) {
            PlannerBenchmarkResult oldResult = singleBenchmarkResult
                    .getSolverBenchmarkResult().getPlannerBenchmarkResult();
            if (!mergeMap.containsKey(oldResult)) {
                if (newResult == null) {
                    newResult = new PlannerBenchmarkResult();
                    newResult.setAggregation(true);
                    newResult.availableProcessors = oldResult.availableProcessors;
                    newResult.loggingLevelOptaPlannerCore = oldResult.loggingLevelOptaPlannerCore;
                    newResult.loggingLevelDroolsCore = oldResult.loggingLevelDroolsCore;
                    newResult.maxMemory = oldResult.maxMemory;
                    newResult.optaPlannerVersion = oldResult.optaPlannerVersion;
                    newResult.javaVersion = oldResult.javaVersion;
                    newResult.javaVM = oldResult.javaVM;
                    newResult.operatingSystem = oldResult.operatingSystem;

                    newResult.parallelBenchmarkCount = oldResult.parallelBenchmarkCount;
                    newResult.warmUpTimeMillisSpentLimit = oldResult.warmUpTimeMillisSpentLimit;
                    newResult.environmentMode = oldResult.environmentMode;
                    newResult.solverBenchmarkResultList = new ArrayList<>();
                    newResult.unifiedProblemBenchmarkResultList = new ArrayList<>();
                    newResult.startingTimestamp = null;
                    newResult.benchmarkTimeMillisSpent = null;
                } else {
                    newResult.availableProcessors = ConfigUtils.mergeProperty(
                            newResult.availableProcessors, oldResult.availableProcessors);
                    newResult.loggingLevelOptaPlannerCore = ConfigUtils.mergeProperty(
                            newResult.loggingLevelOptaPlannerCore, oldResult.loggingLevelOptaPlannerCore);
                    newResult.loggingLevelDroolsCore = ConfigUtils.mergeProperty(
                            newResult.loggingLevelDroolsCore, oldResult.loggingLevelDroolsCore);
                    newResult.maxMemory = ConfigUtils.mergeProperty(
                            newResult.maxMemory, oldResult.maxMemory);
                    newResult.optaPlannerVersion = ConfigUtils.mergeProperty(
                            newResult.optaPlannerVersion, oldResult.optaPlannerVersion);
                    newResult.javaVersion = ConfigUtils.mergeProperty(
                            newResult.javaVersion, oldResult.javaVersion);
                    newResult.javaVM = ConfigUtils.mergeProperty(
                            newResult.javaVM, oldResult.javaVM);
                    newResult.operatingSystem = ConfigUtils.mergeProperty(
                            newResult.operatingSystem, oldResult.operatingSystem);

                    newResult.parallelBenchmarkCount = ConfigUtils.mergeProperty(
                            newResult.parallelBenchmarkCount, oldResult.parallelBenchmarkCount);
                    newResult.warmUpTimeMillisSpentLimit = ConfigUtils.mergeProperty(
                            newResult.warmUpTimeMillisSpentLimit, oldResult.warmUpTimeMillisSpentLimit);
                    newResult.environmentMode = ConfigUtils.mergeProperty(
                            newResult.environmentMode, oldResult.environmentMode);
                }
                mergeMap.put(oldResult, newResult);
            }
        }
        return newResult;
    }

    public static PlannerBenchmarkResult createUnmarshallingFailedResult(String benchmarkReportDirectoryName) {
        PlannerBenchmarkResult result = new PlannerBenchmarkResult();
        result.setName("Failed unmarshalling " + benchmarkReportDirectoryName);
        result.setSolverBenchmarkResultList(Collections.<SolverBenchmarkResult>emptyList());
        result.setUnifiedProblemBenchmarkResultList(Collections.<ProblemBenchmarkResult>emptyList());
        return result;
    }

    @Override
    public String toString() {
        return getName();
    }

}
