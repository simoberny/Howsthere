package it.unitn.simob.howsthere.Helper;

import it.unitn.simob.howsthere.retrofit.models.ForecastResponseModel;

/**
 * Created by
 --Vatsal Bajpai on
 --6/23/2016 at
 --4:29 PM
 */
public abstract class ForecastCallback {

    public abstract void success(ForecastResponseModel response);

    public abstract void failure(String message);
}
