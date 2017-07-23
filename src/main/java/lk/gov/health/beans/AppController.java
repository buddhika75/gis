/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lk.gov.health.beans;

import java.util.List;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import lk.gov.health.dengue.AreaSummery;
import org.primefaces.model.map.MapModel;

/**
 *
 * @author User
 */
@Named(value = "appController")
@ApplicationScoped
public class AppController {

    List<AreaSummery> allAreaSummerys;
    private MapModel provincialDengueMap;
    
    /**
     * Creates a new instance of AppController
     */
    public AppController() {
    }

    public List<AreaSummery> getAllAreaSummerys() {
        return allAreaSummerys;
    }

    public void setAllAreaSummerys(List<AreaSummery> allAreaSummerys) {
        this.allAreaSummerys = allAreaSummerys;
    }

    
    
    public MapModel getProvincialDengueMap() {
        return provincialDengueMap;
    }

    public void setProvincialDengueMap(MapModel provincialDengueMap) {
        this.provincialDengueMap = provincialDengueMap;
    }
    
    public void resetProvincialDengueMap(){
        provincialDengueMap = null;
        allAreaSummerys = null;
    }
    
}
