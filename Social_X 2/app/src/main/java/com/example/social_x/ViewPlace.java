package com.example.social_x;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;


import com.example.social_x.Model.Opening_hours;
import com.example.social_x.Model.PlaceDetail;
import com.example.social_x.Remote.IGoogleAPIService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewPlace extends AppCompatActivity {

    ImageView picture;
    RatingBar rating;
    TextView hours_open;
    TextView address;
    TextView place_name;
    Button btnViewOnMap;

    IGoogleAPIService API_Service;

    PlaceDetail Place;

    EditText addcomment;
    ImageView image_profile;
    TextView post;

    String postid;
    String published;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_place);

        API_Service = GoogleAPIService.getGoogleAPIService();

        picture = (ImageView) findViewById(R.id.photo);
        rating = (RatingBar) findViewById(R.id.ratingBar);
        address = (TextView) findViewById(R.id.place_address);
        place_name = (TextView) findViewById(R.id.place_name);
        hours_open = (TextView) findViewById(R.id.place_open_hour);
        btnViewOnMap = (Button) findViewById(R.id.btn_show_map);


        btnViewOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Place.getResult().getUrl()));
                startActivity(mapIntent);
            }
        });


        //Photos
        if (GoogleAPIService.currentResult.getPhotos() != null && GoogleAPIService.currentResult.getPhotos().length > 0)
        {
            Picasso.with(this).load(getPhotoOfPlace(GoogleAPIService.currentResult.getPhotos()[0].getPhoto_reference(), 1000)).placeholder(R.drawable.ic_baseline_image_24).error(R.drawable.ic_baseline_error_outline_24).into(picture);

            //since getPhotos() is an array we take the first item!
        }

        //Rating
        if(GoogleAPIService.currentResult.getRating() != null && !TextUtils.isEmpty(GoogleAPIService.currentResult.getRating()))
        {
            rating.setRating(Float.parseFloat(GoogleAPIService.currentResult.getRating()));
        }
        else
            {
            rating.setVisibility(View.GONE);
        }
       //this opening hours is for prototype purposes - NEED FIX
        Opening_hours opening_hours = new Opening_hours();
        opening_hours.setOpen_now("9:00am - 5:00pm");
        GoogleAPIService.currentResult.setOpening_hours(opening_hours);
        //Opening Hours
        if(GoogleAPIService.currentResult.getOpening_hours() != null)
        {
            hours_open.setText("Open: " + GoogleAPIService.currentResult.getOpening_hours().getOpen_now());
        }
        else
        {
            hours_open.setVisibility(View.GONE);
        }

        //Address and Name
        API_Service.getDetailPlace(getPlaceDetailUrl(GoogleAPIService.currentResult.getPlace_id()))
                .enqueue(new Callback<PlaceDetail>() {
                    @Override
                    public void onResponse(Call<PlaceDetail> call, Response<PlaceDetail> response) {

                            Place = response.body();

                            address.setText(Place.getResult().getFormatted_address());
                            place_name.setText(Place.getResult().getName());
                        
                    }

                    @Override
                    public void onFailure(Call<PlaceDetail> call, Throwable t) {

                    }
                });

        //FirebaseDatabase database = FirebaseDatabase.getInstance();
        //final DatabaseReference myRef = database.getReference("message");

        //myRef.setValue("");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        addcomment = findViewById(R.id.add_comment);
        //image_profile = findViewById(R.id.image_profile);
        post = findViewById(R.id.post);

        //Intent intent = getIntent();
        //postid = intent.getStringExtra("postid");

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addcomment.getText().toString().equals("")){
                    Toast.makeText(ViewPlace.this, "You cannot post empty comments", Toast.LENGTH_SHORT).show();
                }
                else {


                    //myRef.setValue(addcomment.getText().toString());
                    //addComment();
                    EditText editText = (EditText) findViewById(R.id.add_comment);
                    TextView post = (TextView) findViewById((R.id.comment_text));

                    post.setText((CharSequence)editText.getText());
                    editText.setVisibility(View.INVISIBLE);
                    //view.setVisibility(8);
                    post.setVisibility(View.VISIBLE);
                }
            }
        });


    }


    private void addComment(View view) {
        //DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postid);

        /*HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("comment", addcomment.getText().toString());
        //reference.push().setValue(hashMap);
        addcomment.setText("");*/ //karls code


        //FirebaseDatabase database = FirebaseDatabase.getInstance();
        //DatabaseReference myRef = database.getReference("message");

        //myRef.setValue(s);
        //return myRef; //my fail attempt

        EditText editText = (EditText) this.findViewById(R.id.add_comment);
        TextView post = (TextView) this.findViewById((R.id.post));

        post.setText((CharSequence)editText.getText());
        editText.setVisibility(8);
        view.setVisibility(8);






    }

    private void getComments(String postid, final TextView comments) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Comments").child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                comments.setText("View All" + dataSnapshot.getChildrenCount() + " Comments");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private String getPlaceDetailUrl(String place_id) {

        StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json");
        url.append("?placeid="+place_id);
        url.append("&key="+ getResources().getString(R.string.browser_key));
        return url.toString();
    }

    private String getPhotoOfPlace(String photo_reference, int maxWidth) {
        StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/place/photo");
        url.append("?maxwidth="+maxWidth);
        url.append("&photoreference="+photo_reference);
        url.append("&key="+getResources().getString(R.string.browser_key));
        return url.toString();
    }


}