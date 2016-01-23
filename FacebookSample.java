package com.androidexample.facebook;
 
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
 
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
 
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
 
import org.json.JSONException;
import org.json.JSONObject;
 
@SuppressWarnings("deprecation")
public class AndroidExampleFacebookActivity extends Activity {
 
    /** Añadimos la APP ID de Facebook creado en Facebook.com/developers **/
    private static String APP_ID = "912301902217993"; 
 
    private AsyncFacebookRunner fbAsyncRunner;
    private SharedPreferences fbPrefs;
     
    /** Creamos el objeto de Facebook con el APP ID (Imprescindible) **/
    private Facebook facebook = new Facebook(APP_ID);
     
     
    /** Declaramos botones **/
    Button fbLoginBoton;
    Button fbMiPerfilBoton;
    Button fbAmigoBoton;
    Button fbPublicarMuroBoton;
     
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
 
        fbLoginBoton = (Button) findViewById(R.id.login_btn);
        fbMiPerfilBoton = (Button) findViewById(R.id.profile_btn);
        fbPublicarMuroBoton = (Button) findViewById(R.id.wall_btn);
        fbAmigoBoton = (Button) findViewById(R.id.friend_btn);
        fbAsyncRunner = new AsyncFacebookRunner(facebook);
 
        /** Creamos la acción para click a login **/
        fbLoginBoton.setOnClickListener(new View.OnClickListener() 
        { 
            @Override
            public void onClick(View v)
            {
                /** Llamamos a la función de Login **/
                facebookLogin();
            }
        });
 
         
       /** Información de mi perfil en Facebook **/
        fbMiPerfilBoton.setOnClickListener(new View.OnClickListener()
        { 
            @Override
            public void onClick(View v)
            {
                /** Recogemos los datos del perfil **/
                getFacebookProfileInfo();
            }
        });
 
        /** Publicar en el muro de Facebook **/
        fbPublicarMuroBoton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {                 
                facebookWallPost();
            }
        });
 
        /** Extraemos los amigos de Facebook **/
        fbAmigoBoton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {                 
                getFacebookFriends();
            }
        });
    }
 
    // Función para login
    public void facebookLogin()
    {
         
        /** Extraemos las preferencias para sacar el access_token **/
         
        fbPrefs = getPreferences(MODE_PRIVATE);
        String access_token = fbPrefs.getString("access_token", null);
         
        /** Añadimos el tiempo de expiración del acceso **/
        long expires = fbPrefs.getLong("access_expires", 0);
 
        /** Si tenemos el access_token, continuamos. **/
        if (access_token != null)
        {
            /** Sacamos el access_token para acceder al Graph API de Facebook **/
            facebook.setAccessToken(access_token);
             
            /** Ocultamos el botón de Login y mostramos los de Perfil, Publicar en el muro y Amigos **/
            fbLoginBoton.setVisibility(View.INVISIBLE);

            fbMiPerfilBoton.setVisibility(View.VISIBLE);
            fbPublicarMuroBoton.setVisibility(View.VISIBLE);
            fbAmigoBoton.setVisibility(View.VISIBLE);
        }
 
        if (expires != 0)
        {
            facebook.setAccessExpires(expires);
        }
 
        /** Si todo es correcto y la sesión es válida, sacaremos los parámetros necesarios, se pueden consultar en:
         * https://developers.facebook.com/docs/facebook-login/permissions
        **/

        if (!facebook.isSessionValid())
        {
            facebook.authorize(this,
                new String[] { "email", "publish_stream","user_friends" },
                new DialogListener()
                {
 
                    @Override
                    public void onCancel()
                    {
                        /** Aquí añadiremos las acciones al cancelar el login **/
                    }
 
                    /** Función en caso de ser completado con éxito el login **/

                    @Override
                    public void onComplete(Bundle values)
                    {
                             
                        /** Actualizamos las preferencias con el access_token y el access_expires **/
                        SharedPreferences.Editor editor = fbPrefs.edit();
                        editor.putString("access_token",
                        facebook.getAccessToken());
                        editor.putLong("access_expires",
                        facebook.getAccessExpires());
                        editor.commit();
     
                        /** Ocultamos el botón de Login y mostramos los de Perfil, Publicar en el muro y Amigos **/
                        fbLoginBoton.setVisibility(View.INVISIBLE);
     
                        fbMiPerfilBoton.setVisibility(View.VISIBLE);
                        fbPublicarMuroBoton.setVisibility(View.VISIBLE);
                        fbAmigoBoton.setVisibility(View.VISIBLE);
                             
                    }
 
                    /** Funciones para procesas errores **/

                    @Override
                    public void onError(DialogError error)
                    {
                        /* Aquí añadimos los mensajes de error **/
 
                    }
 
                    @Override
                    public void onFacebookError(FacebookError fberror)
                    {
                        /** Aquí añadimos los mensajes de error que devuelve Facebook **/
                    }
 
                });
        }
    }
 
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        facebook.authorizeCallback(requestCode, resultCode, data);
    }
 
 
    /** Función para extraer información del perfil */
    public void getFacebookProfileInfo()
    {
         
        /** Realizamos la llamada al Graph API de Facebook y le pedimos formato json
        * https://graph.facebook.com/me?access_token=<tu-clave-api>&format=json
        **/
 
        fbAsyncRunner.request("me", new RequestListener()
        {
            @Override
            public void onComplete(String response, Object state)
            {
                Log.d("Perfil", response);
                String json = response;
                try
                {
                    /** Creamos objeot JSON para almacenar datos del perfil **/
                    JSONObject profile = new JSONObject(json);
                     
                    /** Extraemos el nombre de perfil **/
                    final String name = profile.getString("name");                     
                     
                    runOnUiThread(new Runnable()
                    {
 
                        @Override
                        public void run()
                        {
                            /** Mostramos el nombre de perfil **/
                            Toast.makeText(getApplicationContext(), "Mi nombre de perfil: " + name , Toast.LENGTH_LONG).show();
                        }
 
                    });
 
                     
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
 
            @Override
            public void onIOException(IOException e, Object state) {
            }
 
            @Override
            public void onFileNotFoundException(FileNotFoundException e,
                    Object state) {
            }
 
            @Override
            public void onMalformedURLException(MalformedURLException e,
                    Object state) {
            }
 
            @Override
            public void onFacebookError(FacebookError e, Object state) {
            }
        });
    }
 
    /** Función para extraer información de mis amigos de Facebook */
    public void getFacebookFriends() {
         
        /** Realizamos la llamada al Graph API de Facebook y le pedimos formato json
        * https://graph.facebook.com/me?access_token=<tu-clave-api>&format=json
        * Usamos me/friends para extraer los amigos
        **/
 
              fbAsyncRunner.request("me/friends", new RequestListener()
              {

                @Override
                public void onComplete(String response, Object state)
                {
                     
                    Log.d("Número de amigos", response);
                     
                    String json = response;
                     
                    try {
                        /** Creamos objeto JSON para almacenar datos de amigos **/
                        JSONObject profile = new JSONObject(json);
                         
                        /** Extraemos el número de amigos **/
                        final String total_count = profile.getString("summary");
                         
                         
                        runOnUiThread(new Runnable()
                        {

                            @Override
                            public void run()
                            {
                                Toast.makeText(getApplicationContext(), "Número de amigos: " + total_count , Toast.LENGTH_LONG).show();
                            }
 
                        });
 
                         
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
 
                @Override
                public void onIOException(IOException e, Object state) {
                }
 
                @Override
                public void onFileNotFoundException(FileNotFoundException e,
                        Object state) {
                }
 
                @Override
                public void onMalformedURLException(MalformedURLException e,
                        Object state) {
                }
 
                @Override
                public void onFacebookError(FacebookError e, Object state) {
                }
            });
        }
 
     
    /** Función para publicar en el muro de Facebook **/
    public void facebookWallPost()
    {
    
        /** Usamos el campo "feed". Más info: https://developers.facebook.com/docs/graph-api/reference/v2.5/user/feed **/

        facebook.dialog(this, "feed", new DialogListener()
        {
 
            @Override
            public void onFacebookError(FacebookError e) {
            }
 
            @Override
            public void onError(DialogError e) {
            }
 
            @Override
            public void onComplete(Bundle values) {
            }
 
            @Override
            public void onCancel() {
            }
        });
 
    }
 
}