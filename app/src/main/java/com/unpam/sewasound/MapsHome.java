
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
import android.location.LocationManager;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import static android.content.Context.LOCATION_SERVICE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
/**
 * An activity that displays a map showing the place at the device's current location.
 */
public class  MapsHome extends Fragment
        implements OnMapReadyCallback, OnLocationClickListener, PermissionsListener, OnCameraTrackingChangedListener {

    private View rootView;
    private MapView mapView;
    private ArrayList<User> userList;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Mapbox.getInstance(getActivity(), getString(R.string.API_KEY));
        rootView = inflater.inflate(R.layout.maps_with_marker, container, false);
        dbRef = FirebaseDatabase.getInstance().getReference();
        mapView = rootView.findViewById(R.id.map_with_marker_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        return rootView;
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        geoJsonSource = new GeoJsonSource(geojsonSourceLayerId);
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                addUserLocations();
                ReadData();
                setUpSource(style);
                setupLayer(style);
                enableLocationComponent(style);

            }
        });

    }

    public void ReadData(){
        if(LocationService.CheckLocationService(getActivity())){

            userList = new ArrayList<User>();
            dbRef.child("Users").orderByChild("hakAkses").equalTo("Pelapak").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                            final User users = dsp.getValue(User.class);
                            dbRef.child("Alamat").orderByKey().equalTo(dsp.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot alamatData) {
                                    for (DataSnapshot var : alamatData.getChildren()) {
                                        final Alamat alamat = var.getValue(Alamat.class);
                                        users.setAddress(alamat.getAddress());
                                        double lat = alamat.getLat();
                                        double lot = alamat.getLot();
                                        LatLng latLng = new LatLng(lat, lot);
                                        addMarker(latLng, users.getAddress(), null);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }else{

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
    private void addMarker(LatLng latLng, String title, String snippet){
        Icon icon = drawableToIcon(getContext(), R.drawable.mapbox_marker_icon_default, 200);
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
    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(getContext())) {
            if(LocationService.CheckLocationService(getActivity())==true) {
                LocationComponentOptions customLocationComponentOptions = LocationComponentOptions.builder(getContext())
                        .elevation(2)
                        .accuracyAlpha(.6f)
                        .accuracyColor(Color.BLUE)
                        .build();

                // Get an instance of the component
                locationComponent = mapboxMap.getLocationComponent();

                LocationComponentActivationOptions locationComponentActivationOptions =
                        LocationComponentActivationOptions.builder(getContext(), loadedMapStyle)
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

                moveToMyPlace();

                rootView.findViewById(R.id.myLocation).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        moveToMyPlace();
                    }
                });
            }
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }
    }

    private void moveToMyPlace(){
        isInTrackingMode = true;
        locationComponent.setCameraMode(CameraMode.TRACKING);
        locationComponent.zoomWhileTracking(18);
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
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
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
    public void onDestroy() {
        super.onDestroy();

        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
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
            Toast.makeText(getContext(), String.format(getString(R.string.current_location),
                    locationComponent.getLastKnownLocation().getLatitude(),
                    locationComponent.getLastKnownLocation().getLongitude()), Toast.LENGTH_LONG).show();
        }
    }
}