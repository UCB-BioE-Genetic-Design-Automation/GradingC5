package org.ucb.bio134.gradingc5;

/**
    PriceCalculatorTest:28.346000000000007
    SemiprotocolPriceSimulatorTest:32.04
    BasicConversionTest:7.4515
    MastermixTest:163.46300000000002
    OptimizationTest:573.149
    * 
 * @author J. Christopher Anderson
 */

class Prices {
    Double PriceCalculatorTest;
    Double SemiprotocolPriceSimulatorTest;
    Double BasicConversionTest;
    Double MastermixTest;
    Double OptimizationTest;
    
    public Prices(String text) throws Exception {
        String[] lines = text.trim().split("\\r|\\r?\\n");
        for(int i=0; i<lines.length; i++) {
            String aline = lines[i].trim();
            if(aline.isEmpty()) {
                continue;
            }
            String[] colons = aline.split(":");
            Double val = Double.parseDouble(colons[1]);
            if(colons[0].equals("PriceCalculatorTest")) {
                PriceCalculatorTest = val;
            } else if(colons[0].equals("SemiprotocolPriceSimulatorTest")) {
                SemiprotocolPriceSimulatorTest = val;
            } else if(colons[0].equals("BasicConversionTest")) {
                BasicConversionTest = val;
            } else if(colons[0].equals("MastermixTest")) {
                MastermixTest = val;
            } else if(colons[0].equals("OptimizationTest")) {
                OptimizationTest = val;
            } else {
                System.err.println(colons[0] + "   -    " + colons[1]);
                throw new Exception();
            }
        }
    }
    
    public static void main(String[] args) throws Exception {
        Prices prices = new Prices("" +
            "PriceCalculatorTest:28.346000000000007\n" +
            "SemiprotocolPriceSimulatorTest:32.04\n" +
            "BasicConversionTest:7.4515\n" +
            "MastermixTest:163.46300000000002\n" +
            "OptimizationTest:573.149");
        
        System.out.println(prices.OptimizationTest);
    }
}
