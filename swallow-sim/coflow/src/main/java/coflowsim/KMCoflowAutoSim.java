package coflowsim;

import coflowsim.simulators.*;
import coflowsim.traceproducers.CoflowBenchmarkTraceProducer;
import coflowsim.traceproducers.CustomTraceProducer;
import coflowsim.traceproducers.JobClassDescription;
import coflowsim.traceproducers.TraceProducer;
import coflowsim.utils.Constants;
import coflowsim.utils.Constants.SHARING_ALGO;
import coflowsim.simulators.KMCoflowAutoSimulatorSmartCompression;



public class KMCoflowAutoSim {

    public static void main(String[] args) {

//        double[]  bandwidthArr          =  {100, 500, 1000, 5000, 10000};
//        double[]  cpuIdleThresholdArr   =  {0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55, 0.6, 0.65, 0.7, 0.75, 0.8, 0.85, 0.9, 0.95};

//        double[]  bandwidthArr          =  {1000, 1100, 1200, 1300, 1400, 1500, 2000, 2500, 3000, 3500, 4000, 4500, 5000};
//        double[]  cpuIdleThresholdArr   =  {0.05, 0.5, 0.95};

//        double[]  bandwidthArr          =  {1500, 1600, 1700, 1800, 1900, 2000, 2100, 2200, 2300, 2400, 2500, 2600, 2700, 2800, 2900, 3000};
//        double[]  cpuIdleThresholdArr   =  {0.05, 0.5, 0.95};

//        double[]  bandwidthArr          =  {10, 20, 40, 60, 80, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000};
//        double[]  cpuIdleThresholdArr   =  {0.05, 0.5, 0.95};

        double[]  bandwidthArr          =  {10};
        double[]  cpuIdleThresholdArr   =  {0.5};

        boolean[] enforceCompressionArr =  {false};

        for (int bandwidthIndex = 0; bandwidthIndex < bandwidthArr.length; bandwidthIndex++){
            for (int cpuIndex = 0; cpuIndex < cpuIdleThresholdArr.length; cpuIndex++) {
                for (int ecIndex = 0; ecIndex < enforceCompressionArr.length; ecIndex++) {
                    String bandwidthInfo = "Bandwidth:                 " + bandwidthArr[bandwidthIndex]       + " Mbps"   + "\n";
                    String cpuInfo       = "CPU Idle Threshold:        " + cpuIdleThresholdArr[cpuIndex]*100  + " %"      + "\n";
                    String ecInfo        = "Is Enforced COmpression:   " + enforceCompressionArr[ecIndex]                 + "\n";
                    String hardwareInfo  = bandwidthInfo + cpuInfo + ecInfo;

                    KMLogCenter.INSTANCE.addLog(hardwareInfo);



                    int curArg = 0;

                    SHARING_ALGO sharingAlgo = SHARING_ALGO.FVDF;
                    if (args.length > curArg) {
                        String UPPER_ARG = args[curArg++].toUpperCase();

                        if (UPPER_ARG.contains("FAIR") || UPPER_ARG.contains("PFF")) {
                            sharingAlgo = SHARING_ALGO.FAIR;
                        } else if (UPPER_ARG.contains("PFP")) {
                            sharingAlgo = SHARING_ALGO.PFP;
                        } else if (UPPER_ARG.contains("FIFO")) {
                            sharingAlgo = SHARING_ALGO.FIFO;
                        } else if (UPPER_ARG.contains("SCF") || UPPER_ARG.contains("SJF")) {
                            sharingAlgo = SHARING_ALGO.SCF;
                        } else if (UPPER_ARG.contains("NCF") || UPPER_ARG.contains("NJF")) {
                            sharingAlgo = SHARING_ALGO.NCF;
                        } else if (UPPER_ARG.contains("LCF") || UPPER_ARG.contains("LJF")) {
                            sharingAlgo = SHARING_ALGO.LCF;
                        } else if (UPPER_ARG.contains("SEBF")) {
                            sharingAlgo = SHARING_ALGO.SEBF;
                        } else if (UPPER_ARG.contains("DARK")) {
                            sharingAlgo = SHARING_ALGO.DARK;
                        } else if (UPPER_ARG.contains("SSCF")) {
                            sharingAlgo = SHARING_ALGO.SSCF;
                        } else if (UPPER_ARG.contains("FVDF")) {
                            sharingAlgo = SHARING_ALGO.FVDF;
                        }
                        else {
                            System.err.println("Unsupported or Wrong Sharing Algorithm");
                            System.exit(1);
                        }
                    }

                    boolean isOffline = false;
                    int simulationTimestep = 10 * Constants.SIMULATION_SECOND_MILLIS;
                    if (isOffline) {
                        simulationTimestep = Constants.SIMULATION_ENDTIME_MILLIS;
                    }

                    boolean considerDeadline = false;
                    double deadlineMultRandomFactor = 1;
                    if (considerDeadline && args.length > curArg) {
                        deadlineMultRandomFactor = Double.parseDouble(args[curArg++]);
                    }

                    // Create TraceProducer
                    TraceProducer traceProducer = null;

                    int numRacks = 100;
                    int numJobs = 10;
                    int randomSeed = 13;
                    JobClassDescription[] jobClassDescs = new JobClassDescription[] {
                            new JobClassDescription(1, 5, 1, 10),
                            new JobClassDescription(1, 5, 10, 1000),
                            new JobClassDescription(5, numRacks, 1, 10),
                            new JobClassDescription(5, numRacks, 10, 1000) };
                    double[] fracsOfClasses = new double[] {
                            41,
                            29,
                            9,
                            21 };

                    traceProducer = new CustomTraceProducer(numRacks, numJobs, jobClassDescs, fracsOfClasses,
                            randomSeed);

                    if (args.length > curArg) {
                        String UPPER_ARG = args[curArg++].toUpperCase();

                        if (UPPER_ARG.equals("CUSTOM")) {
                            int numClasses = Integer.parseInt(args[curArg++]);

                            jobClassDescs = new JobClassDescription[numClasses];
                            for (int i = 0; i < numClasses; i++) {
                                int minW = Integer.parseInt(args[curArg++]);
                                int maxW = Integer.parseInt(args[curArg++]);
                                int minL = Integer.parseInt(args[curArg++]);
                                int maxL = Integer.parseInt(args[curArg++]);

                                jobClassDescs[i] = new JobClassDescription(minW, maxW, minL, maxL);
                            }

                            fracsOfClasses = new double[numClasses];
                            for (int i = 0; i < numClasses; i++) {
                                fracsOfClasses[i] = Integer.parseInt(args[curArg++]);
                            }

                            numRacks = Integer.parseInt(args[curArg++]);
                            numJobs = Integer.parseInt(args[curArg++]);
                            randomSeed = Integer.parseInt(args[curArg++]);

                            traceProducer = new CustomTraceProducer(numRacks, numJobs, jobClassDescs, fracsOfClasses,
                                    randomSeed);
                        } else if (UPPER_ARG.equals("COFLOW-BENCHMARK")) {
                            String pathToCoflowBenchmarkTraceFile = args[curArg++];
                            traceProducer = new CoflowBenchmarkTraceProducer(pathToCoflowBenchmarkTraceFile);
                        }
                    }
                    traceProducer.prepareTrace();

                    Simulator nlpl = null;
                    if (sharingAlgo == SHARING_ALGO.FAIR || sharingAlgo == SHARING_ALGO.PFP) {
                        nlpl = new FlowSimulator(sharingAlgo, traceProducer, isOffline, considerDeadline,
                                deadlineMultRandomFactor);

                        nlpl.simulate(simulationTimestep);
                        nlpl.printStats(true);
                    }
                    else if (sharingAlgo == SHARING_ALGO.DARK) {
                        nlpl = new CoflowSimulatorDark(sharingAlgo, traceProducer);

                        nlpl.simulate(simulationTimestep);
                        nlpl.printStats(true);
                    }
                    else if (sharingAlgo == SHARING_ALGO.SSCF || sharingAlgo == SHARING_ALGO.FVDF) {

                        KMCoflowAutoSimulatorSmartCompression sim = new KMCoflowAutoSimulatorSmartCompression(SHARING_ALGO.SEBF,
                                traceProducer, isOffline, considerDeadline, deadlineMultRandomFactor);

                        sim.simBandwidth = bandwidthArr[bandwidthIndex];
                        sim.simCPUIdleThreshold = cpuIdleThresholdArr[cpuIndex];
                        sim.enforceCompression = enforceCompressionArr[ecIndex];

                        sim.simulate(simulationTimestep);
                        sim.printStats(false);
                    }
                    else {
                        nlpl = new CoflowSimulator(sharingAlgo, traceProducer, isOffline, considerDeadline,
                                deadlineMultRandomFactor);

                        nlpl.simulate(simulationTimestep);
                        nlpl.printStats(true);
                    }

                } // enforceCompression scope
            } // cpu scope
        } // bandwidth scope

        KMLogCenter.INSTANCE.description();
        KMLogCenter.INSTANCE.saveLog();
    } // main()
} // class domain
