
package com.unpam.sewasound;


import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Parcel;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.JsonObject;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.android.gestures.Utils;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.BaseMarkerOptions;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener;
import com.mapbox.mapboxsdk.location.OnLocationClickListener;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerView;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.plugins.places.picker.PlacePicker;
import com.mapbox.mapboxsdk.plugins.places.picker.model.PlacePickerOptions;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import java.util.List;
import java.util.Locale;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
/**
 * An activity that displays a map showing the place at the device's current location.
 */
public class  Maps2Activity extends AppCompatActivity
        implements OnMapReadyCallback, OnLocationClickListener, PermissionsListener, OnCameraTrackingChangedListener {
    private MapView mapView;
    private String TAG = "Search";
    private GeoJsonSource geoJsonSource;
    private ValueAnimator animator;
    private PermissionsManager permissionsManager;
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private MapboxMap mapboxMap;
    private CarmenFeature home;
    private CarmenFeature work;
    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    private String symbolIconId = "symbolIconId";
    private LocationComponent locationComponent;
    private boolean isInTrackingMode;
    private boolean lockThisLocation;
    private int PLACE_SELECTION_REQUEST_CODE = 5678;
    DatabaseReference dbRef;
    private Marker marker;
    private ImageView hoveringMarker;
    private String title="", snippet="";
    private FloatingActionButton lockLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.API_KEY));

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_maps);
        dbRef = FirebaseDatabase.getInstance().getReference("Alamat");
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }


    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        hoveringMarker = new ImageView(Maps2Activity.this);
        hoveringMarker.setImageResource(R.drawable.map_default_map_marker);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        hoveringMarker.setLayoutParams(params);
        mapView.addView(hoveringMarker);

        geoJsonSource = new GeoJsonSource(geojsonSourceLayerId);
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                initSearchFab();
                MyMarker();
                addUserLocations();
                fabSetAlamat();
                setUpSource(style);
                setupLayer(style);
                enableLocationComponent(style);

            }
        });

    }

    public void MyMarker(){
        lockLocation = (FloatingActionButton) findViewById(R.id.fab_lock_location);
        lockLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //lockThisLocation = true;
                if(lockThisLocation == false){
                    hoveringMarker.setVisibility(View.VISIBLE);
                    if(hoveringMarker.getVisibility()==View.VISIBLE){
                        lockLocation.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                    }
                    mapboxMap.clear();
                    lockThisLocation = true;
                }else{
                    LocationService.setCurrentLocation( mapboxMap.getCameraPosition().target);
                    title = getCompleteAddressString(LocationService.getCurrentLocation().getLatitude(), LocationService.getCurrentLocation().getLongitude());
                    addMarker(LocationService.getCurrentLocation(), title, snippet);
                    hoveringMarker.setVisibility(View.INVISIBLE);
                    if(hoveringMarker.getVisibility()==View.INVISIBLE){
                        lockLocation.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.mapbox_plugins_light_navy)));
                    }
                    lockThisLocation = false;
                }
            }
        });
    }

    private void fabSetAlamat(){
        findViewById(R.id.setMyLocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Maps2Activity.this, PenyewaanActivity.class);
                i.putExtra("title", title);
                setResult(RESULT_OK, i);
                finish();
            }
        });
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            if(LocationService.CheckLocationService(this)==true) {
                LocationComponentOptions customLocationComponentOptions = LocationComponentOptions.builder(this)
                        .elevation(2)
                        .accuracyAlpha(.6f)
                        .accuracyColor(Color.BLUE)
                        .build();

                // Get an instance of the component
                locationComponent = mapboxMap.getLocationComponent();

                LocationComponentActivationOptions locationComponentActivationOptions =
                        LocationComponentActivationOptions.builder(this, loadedMapStyle)
                                .locationComponentOptions(customLocationComponentOptions)
                                .build();

                // Activate with options
                locationComponent.activateLocationComponent(locationComponentActivationOptions);

                // Enable to make component visible
                locationComponent.setLocationComponentEnabled(true);

                // Set the component's camera mode
                locationComponent.setCameraMode(CameraMode.TRACKING);

                // Set the component's render mode
                locationComponent.setRenderMode(RenderMode.COMPASS);

                // Add the location icon click listener
                locationComponent.addOnLocationClickListener(this);

                // Add the camera tracking listener. Fires if the map camera is manually moved.
                locationComponent.addOnCameraTrackingChangedListener(this);

                LocationService.setCurrentLocation(new LatLng(locationComponent.getLastKnownLocation().getLatitude(), locationComponent.getLastKnownLocation().getLongitude()));
                title = getCompleteAddressString(locationComponent.getLastKnownLocation().getLatitude(), locationComponent.getLastKnownLocation().getLongitude());
                moveToMyPlace();

                findViewById(R.id.myLocation).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        moveToMyPlace();
                    }
                });
            }
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    private void moveToMyPlace(){
        isInTrackingMode = true;
        locationComponent.setCameraMode(CameraMode.TRACKING);
        locationComponent.zoomWhileTracking(18);
    }

    private void initSearchFab() {
        LatLng latLng = LocationService.getCurrentLocation();
        findViewById(R.id.fab_location_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(Mapbox.getAccessToken())
                        .placeOptions(PlaceOptions.builder()
                                .backgroundColor(Color.parseColor("#EEEEEE"))
                                .country("id")
                                .build(PlaceOptions.CONTENTS_FILE_DESCRIPTOR))
                        .build(Maps2Activity.this);
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
            }
        });
    }



    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i));
                }
                strAdd = strReturnedAddress.toString();
                //Log.w("My Current loction address", strReturnedAddress.toString());
            } else {
                //Log.w("My Current loction address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Log.w("My Current loction address", "Canont get Address!");
        }
        return strAdd;
    }
    private void addUserLocations() {
        home = CarmenFeature.builder().text("Mapbox SF Office")
                .geometry(Point.fromLngLat(-122.3964485, 37.7912561))
                .placeName("50 Beale St, San Francisco, CA")
                .id("mapbox-sf")
                .properties(new JsonObject())
                .build();

        work = CarmenFeature.builder().text("Mapbox DC Office")
                .placeName("740 15th Street NW, Washington DC")
                .geometry(Point.fromLngLat(-77.0338348, 38.899750))
                .id("mapbox-dc")
                .properties(new JsonObject())
                .build();
    }

    private void setUpSource(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addSource(geoJsonSource);
    }

    private void setupLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addLayer(new SymbolLayer("SYMBOL_LAYER_ID", geojsonSourceLayerId).withProperties(
                iconImage(symbolIconId),
                iconIgnorePlacement(true),
                iconAllowOverlap(true)
        ));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mapboxMap != null) {
            Style style = mapboxMap.getStyle();

            if (style != null) {

                if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {
                    CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);


                    //geoJsonSource = style.getSourceAs(geojsonSourceLayerId);
                    if (geoJsonSource != null) {
                        geoJsonSource.setGeoJson(FeatureCollection.fromFeatures(
                                new Feature[]{Feature.fromJson(selectedCarmenFeature.toJson())}));
                    }
                    if (selectedCarmenFeature != null) {
                        LocationService.setCurrentLocation(new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                                ((Point) selectedCarmenFeature.geometry()).longitude()));
                    }

                    // Move map camera to the selected location
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(LocationService.getCurrentLocation())
                                    .zoom(18)
                                    .build()), 4000);
                    title = getCompleteAddressString(((Point) selectedCarmenFeature.geometry()).latitude(), ((Point) selectedCarmenFeature.geometry()).longitude());
                    //snippet = selectedCarmenFeature.address();
                    if(mapboxMap.getMarkers() != null){
                        mapboxMap.clear();
                    }
                    addMarker(LocationService.getCurrentLocation(), title, snippet);
                }


            }
        }
    }

    private void addMarker(LatLng latLng, String title, String snippet){
        Icon icon = drawableToIcon(getApplicationContext(), R.drawable.mapbox_marker_icon_default, 200);
        if(marker != null){
            marker.remove();
        }
        marker = mapboxMap.addMarker(new MarkerOptions().position(latLng).title(title).icon(icon));

    }

    public Icon drawableToIcon(@NonNull Context context, @DrawableRes int id, int size) {

        Drawable vectorDrawable = AppCompatResources.getDrawable(context, id);
        if (vectorDrawable == null)
            return null;
        Bitmap bitmap = Bitmap.createBitmap((size/10)*4, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return IconFactory.getInstance(context).fromBitmap(bitmap);
    }
    // Add the mapView lifecycle to the activity's lifecycle methods
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {

    }

    @Override
    public void onCameraTrackingDismissed() {

    }

    @Override
    public void onCameraTrackingChanged(int currentMode) {

    }

    @Override
    public void onLocationComponentClick() {
        if (locationComponent.getLastKnownLocation() != null) {
            Toast.makeText(this, String.format(getString(R.string.current_location),
                    locationComponent.getLastKnownLocation().getLatitude(),
                    locationComponent.getLastKnownLocation().getLongitude()), Toast.LENGTH_LONG).show();
        }
    }
}