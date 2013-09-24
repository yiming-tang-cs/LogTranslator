package cz.muni.fi.ngmon.logchanger;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mtoth
 */
public class TestingClass {

    
    Logger LOG = Logger.getLogger(TestingClass.class.toString());
    
    private int number;
    
    /*  Some comment spread over 
        two lines for fun.
    */
    public TestingClass(int number) {
        this.number = number;
    }
    
    // one liner comment
    public void add(int number) {
        // comment inside method
        LOG.info("Adding");
        this.number += number;
    }
    
    public void substract(int number) {  
        LOG.log(Level.INFO, "Substracting {0}", number);
        this.number -= number;
    }
    
    public void multiply(int number) {
        this.number *= number;       
    }
    
    public int getNumber() {
        return this.number;
    }
    
}
