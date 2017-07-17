/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lk.gov.health.schoolhealth;

/**
 *
 * @author User
 */
public enum PrivilegeType {
    //System Level
    System_Administrator,
    System_Super_User,
    @Deprecated
    Institution_Administrator,
    @Deprecated
    Institution_Super_User,
    //Provincial Level
    PDHS,
    CCP_PDHS,
    MO_PDHS,
    PSPHNS,
    PSPHI,
    PSPHM,
    PDHS_Staff,
    //District Level
    RDHS,
    CCP_RDHS,
    MO_School_Health,
    MO_RDHS,
    MO_MCH,
    Regional_Epidemiologist,
    DSPHNS,
    DSPHI,
    DSPHM,
    RSPHNO,
    SPHID,
    SSDT,
    MO_Planning_RDHS,
    RDHS_Staff,
    Triposha_Limited,
    //MOH Level
    MOH,
    AMOH,
    MO,
    RMO_AMO,
    SPHNS,
    SPHI,
    SPHM,
    MOH_Staff,
    //Guest Level
    Guest,
}
