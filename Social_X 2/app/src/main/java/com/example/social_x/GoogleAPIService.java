package com.example.social_x;

import com.example.social_x.Model.MyPlaces;
import com.example.social_x.Model.Results;
import com.example.social_x.Remote.IGoogleAPIService;
import com.example.social_x.Remote.RetrofitClient;

public class GoogleAPIService {

    public static Results currentResult;

    public static final String GOOGLE_API_URL = "https://maps.googleapis.com/";

    public static IGoogleAPIService getGoogleAPIService()
    {
        return RetrofitClient.getClient(GOOGLE_API_URL).create(IGoogleAPIService.class);
    }
}
