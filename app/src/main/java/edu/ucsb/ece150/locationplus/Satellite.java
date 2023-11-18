package edu.ucsb.ece150.locationplus;

/*
 * This class is provided as a way for you to store information about a single satellite. It can
 * be helpful if you would like to maintain the list of satellites using an ArrayList (i.e.
 * ArrayList<Satellite>). As in Homework 3, you can then use an Adapter to update the list easily.
 *
 * You are not required to implement this if you want to handle satellite information in using
 * another method.
 */

public class Satellite {

    private int prn; // Pseudo-Random Noise (PRN) code
    private float azimuthDegrees;
    private float elevationDegrees;
    private float cn0DbHz; // Carrier-to-Noise Density in dB-Hz
    private float carrierFrequency; // Carrier Frequency in Hz
    private String constellationName; // Constellation Name
    private int svid; // Satellite Vehicle ID

    public Satellite(int prn, float azimuthDegrees, float elevationDegrees, float cn0DbHz,
                        float carrierFrequency, String constellationName, int svid) {
        this.prn = prn;
        this.azimuthDegrees = azimuthDegrees;
        this.elevationDegrees = elevationDegrees;
        this.cn0DbHz = cn0DbHz;
        this.carrierFrequency = carrierFrequency;
        this.constellationName = constellationName;
        this.svid = svid;
    }

    public int getPrn() {
        return prn;
    }

    public float getAzimuthDegrees() {
        return azimuthDegrees;
    }

    public float getElevationDegrees() {
        return elevationDegrees;
    }

    public float getCn0DbHz() {
        return cn0DbHz;
    }

    @Override
    public String toString() {
        return "SVID: " + prn +
                "\nAzimuth: " + azimuthDegrees + "°" +
                "\nElevation: " + elevationDegrees + "°" +
                "\nCarrier Frequency: " + carrierFrequency + " Hz" +
                "\nC/N0: " + cn0DbHz + " dB-Hz" +
                "\nConstellation: " + constellationName;
    }

    public void setAzimuthDegrees(float azimuthDegrees) {
        this.azimuthDegrees = azimuthDegrees;
    }

    public void setElevationDegrees(float elevationDegrees) {
        this.elevationDegrees = elevationDegrees;
    }

    public void setCn0DbHz(float cn0DbHz) {
        this.cn0DbHz = cn0DbHz;
    }
}
