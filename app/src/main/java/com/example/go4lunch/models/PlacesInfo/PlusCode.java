package com.example.go4lunch.models.PlacesInfo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Adrien Deguffroy on 20/07/2018.
 */
public class PlusCode {
    @SerializedName("compound_code")
    @Expose
    public String compoundCode;
    @SerializedName("global_code")
    @Expose
    public String globalCode;

    public String getCompoundCode() {
        return compoundCode;
    }

    public void setCompoundCode(String compoundCode) {
        this.compoundCode = compoundCode;
    }

    public String getGlobalCode() {
        return globalCode;
    }

    public void setGlobalCode(String globalCode) {
        this.globalCode = globalCode;
    }
}