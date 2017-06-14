package in.codelab.androidauthdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import timber.log.Timber;

public class MainActivity
        extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    SignInButton signInButton;
    private Button signOutButton;
    TextView statusTextView;
    GoogleApiClient mGoogleApiClient;
    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener authStateListener;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {
                    Timber.d("Current loggedin user is %s", currentUser.getDisplayName());
                } else {
                    Timber.d("User signed out..");
                }
            }
        };

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .enableAutoManage(this , this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        statusTextView = (TextView) findViewById(R.id.status_textview);
        signInButton = (SignInButton) findViewById(R.id.gsign_in_button);
        signInButton.setOnClickListener(this);
        signOutButton = (Button) findViewById(R.id.sign_out_button);
        signOutButton.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.gsign_in_button:
                signIn();
                break;
            case R.id.sign_out_button:
                signOut();
                break;
            default:
                Timber.d("Unknown id clicked : %d", v.getId());
                break;
        }
    }

    private void signOut() {
        Timber.d("Signing out...");
        firebaseAuth.signOut();
        updateUIState();
/*        PendingResult<Status> statusPendingResult = Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        statusPendingResult.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if(status.isSuccess()) {
                    firebaseAuth.signOut();
                    Toast.makeText(MainActivity.this, status.getStatusMessage(), Toast.LENGTH_SHORT)
                            .show();
                    signOutButton.setVisibility(View.INVISIBLE);
                    signInButton.setVisibility(View.VISIBLE);
                    statusTextView.setText("Hello World!");
                } else {
                    Toast.makeText(MainActivity.this, status.getStatusMessage(), Toast.LENGTH_LONG)
                            .show();
                }
            }
        });*/
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs won't be available.
        Timber.d( "onConnectionFailed: %s", connectionResult.getErrorMessage());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //  It's possible to have multiple activities calling back,
        // so the code (RC_SIGN_IN here) is used to differentiate between them.
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            firebaseAuthWithGoogle(acct);
        } else {
            Timber.d("Error signing-in with Google account.."+ result.getStatus().getStatusMessage());
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Firebase-Sign in success, update UI with the signed-in user's information
                            Timber.d("Successfully signed into Firebase with Google acct");
                            updateUIState();
                        } else {
                            Timber.d("Error signing-into Firebase with Google credentials..");
                        }
                    }
                });
    }

    private void updateUIState() {
        if (firebaseAuth != null && firebaseAuth.getCurrentUser() != null) {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            statusTextView.setText("Hello, " + currentUser.getDisplayName()); //pulls the user name
            signInButton.setVisibility(View.INVISIBLE);
            signOutButton.setVisibility(View.VISIBLE);
        } else {
            statusTextView.setText("Hello, Anonymous!");
            signInButton.setVisibility(View.VISIBLE);
            signOutButton.setVisibility(View.INVISIBLE);
        }
    }
}
