package com.lechelaimperial.controlaccesos;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import com.lechelaimperial.controlaccesos.backend.entity.SysConfig;
import com.lechelaimperial.controlaccesos.backend.repository.AppDatabase;
import com.lechelaimperial.controlaccesos.pojo.Post;
import com.lechelaimperial.controlaccesos.restAPI.EndPointsApi;
import com.lechelaimperial.controlaccesos.restAPI.adaptador.RestApiAdaptador;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccesoActivity extends AppCompatActivity implements View.OnClickListener {

  private String sCompany = null;
  private TextView txtBienvenida;
  private EditText etCodigo;
  private TextView tvNombre;
  private EditText tvHora;
  //private TextView tvFecha;
  private ImageView ivFoto;
  private ImageView ivFotoLove;
  private ImageView ivFotoEmpresa;


  //private LinearLayout lyDetalleAcceso;
  private TextView tvHoraActual;

  private DecoratedBarcodeView barcodeView;
  private BeepManager beepManager;
  private String lastText;

  private TextClock textClock;
  AppDatabase appDatabase;
  Dialog dialog;

  // Barcode que funciona
  private BarcodeCallback callback = new BarcodeCallback() {
    @Override
    public void barcodeResult(BarcodeResult result) {
      if (result.getText() == null || result.getText().equals(lastText)) {
        // Prevent duplicate scans
        return;
      }

      String employeeAndCompany = result.getText();
      String employeeNumber = result.getText();
      if (employeeNumber.length() == 3) {
        configurarEmpresa(employeeNumber);
        return;
      } else if (employeeNumber.length() == 4) {
        sCompany = obtenerEmpresa();
        if (sCompany != null) {
          employeeAndCompany = sCompany + employeeNumber;
        } else {
          Toast.makeText(getBaseContext(), "La Empresa no esta configurada", Toast.LENGTH_LONG).show();
        }
      } else if (employeeNumber.length() == 7) {
        sCompany = employeeNumber.substring(0, 3);
        employeeNumber = employeeNumber.substring(3, 7);
      }

      lastText = result.getText();
      barcodeView.setStatusText(result.getText());
      etCodigo.setText(employeeNumber);
      registrarAccesoWS(employeeAndCompany);
      //lyDetalleAcceso.setVisibility(View.VISIBLE);
      beepManager.playBeepSoundAndVibrate();
    }

    @Override
    public void possibleResultPoints(List<ResultPoint> resultPoints) {
    }
  };


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_acceso);
    int MY_PERMISSIONS_REQUEST_CAMERA = 0;
// Here, this is the current activity
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
      if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {

      } else {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
        // app-defined int constant. The callback method gets the
        // result of the request.
      }
    }


    textClock = (TextClock) findViewById(R.id.textClock);
    txtBienvenida = (TextView) findViewById(R.id.txtBienvenida);

    //tvHoraActual = (TextView) findViewById(R.id.tvHoraActual);
    etCodigo = (EditText) findViewById(R.id.etCodigo);
    etCodigo.setEnabled(false);
    tvNombre = (TextView) findViewById(R.id.tvNombre);
    tvHora = (EditText) findViewById(R.id.tvHora);
    // tvFecha  = (TextView) findViewById(R.id.tvFecha);
    ivFoto = (ImageView) findViewById(R.id.ivFoto);
    ivFotoLove = (ImageView) findViewById(R.id.ivFotoLove);
    ivFotoEmpresa = (ImageView) findViewById(R.id.ivFotoEmpresa);
    dialog = new Dialog(this);
    //lyDetalleAcceso = (LinearLayout) findViewById(R.id.lyDetalleAcceso);
    //lyDetalleAcceso.setVisibility(View.INVISIBLE);

    barcodeView = findViewById(R.id.barcode_scanner);
    Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39);
    barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
    barcodeView.initializeFromIntent(getIntent());
    barcodeView.decodeContinuous(callback);

    beepManager = new BeepManager(this);

    etCodigo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (i == EditorInfo.IME_ACTION_DONE || i == EditorInfo.IME_ACTION_NEXT) {
          String numeroEmpleado = etCodigo.getText().toString();
          etCodigo.setError(null);
          if ("".equals(numeroEmpleado)) {
            etCodigo.setError("INTRODUCE UN CODIGO VALIDO");
            refreshInterface();
          } else {
            registrarAccesoWS(etCodigo.getText().toString());
            //.setVisibility(View.VISIBLE);
          }
        }
        return false;
      }
    });

    //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    //String currentDateandTime = simpleDateFormat.format(new Date());
    //tvHoraActual.setText(currentDateandTime);


    PackageManager pm = this.getPackageManager();
    String packageName = this.getPackageName();
    int flags = PackageManager.GET_SIGNATURES;

    PackageInfo packageInfo = null;

    try {
      packageInfo = pm.getPackageInfo(packageName, flags);
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
    Signature[] signatures = packageInfo.signatures;

    byte[] cert = signatures[0].toByteArray();

    InputStream input = new ByteArrayInputStream(cert);

    CertificateFactory cf = null;
    try {
      cf = CertificateFactory.getInstance("X509");


    } catch (CertificateException e) {
      e.printStackTrace();
    }
    X509Certificate c = null;
    try {
      c = (X509Certificate) cf.generateCertificate(input);
    } catch (CertificateException e) {
      e.printStackTrace();
    }


    try {
      MessageDigest md = MessageDigest.getInstance("SHA1");
      byte[] publicKey = md.digest(c.getPublicKey().getEncoded());


      StringBuffer hexString = new StringBuffer();
      for (int i = 0; i < publicKey.length; i++) {
        String appendString = Integer.toHexString(0xFF & publicKey[i]);
        if (appendString.length() == 1) hexString.append("0");
        hexString.append(appendString);
      }


      Log.d("Example", "Cer: " + hexString.toString());

    } catch (NoSuchAlgorithmException e1) {
      e1.printStackTrace();
    }
  }


  private String getCertificateSHA1Fingerprint() {
    PackageManager pm = this.getPackageManager();
    String packageName = this.getPackageName();
    int flags = PackageManager.GET_SIGNATURES;
    PackageInfo packageInfo = null;
    try {
      packageInfo = pm.getPackageInfo(packageName, flags);
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
    Signature[] signatures = packageInfo.signatures;
    byte[] cert = signatures[0].toByteArray();
    InputStream input = new ByteArrayInputStream(cert);
    CertificateFactory cf = null;
    try {
      cf = CertificateFactory.getInstance("X509");
    } catch (CertificateException e) {
      e.printStackTrace();
    }
    X509Certificate c = null;
    try {
      c = (X509Certificate) cf.generateCertificate(input);
    } catch (CertificateException e) {
      e.printStackTrace();
    }
    String hexString = null;
    try {
      MessageDigest md = MessageDigest.getInstance("SHA1");
      byte[] publicKey = md.digest(c.getEncoded());
      hexString = byte2HexFormatted(publicKey);
    } catch (NoSuchAlgorithmException e1) {
      e1.printStackTrace();
    } catch (CertificateEncodingException e) {
      e.printStackTrace();
    }
    return hexString;
  }

  public static String byte2HexFormatted(byte[] arr) {
    StringBuilder str = new StringBuilder(arr.length * 2);
    for (int i = 0; i < arr.length; i++) {
      String h = Integer.toHexString(arr[i]);
      int l = h.length();
      if (l == 1) h = "0" + h;
      if (l > 2) h = h.substring(l - 2, l);
      str.append(h.toUpperCase());
      if (i < (arr.length - 1)) str.append(':');
    }
    return str.toString();
  }

  public void registrarAccesoWS(String codigo) {

    try {
      // Crea una instancia de tu TrustManager personalizado
      TrustManager[] trustManagers = new TrustManager[]{new CustomTrustManager()};

      // Configura un contexto SSL con tu TrustManager personalizado
      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(null, trustManagers, null);

      // Establece el SocketFactory personalizado en las conexiones HTTPS
      HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
    } catch (Exception e) {
      e.printStackTrace();
      if (e instanceof SSLHandshakeException) {
        Throwable cause = e.getCause();
        if (cause instanceof CertPathValidatorException) {
          CertPathValidatorException certPathException = (CertPathValidatorException) cause;
          System.out.println("CertPathValidatorException: " + certPathException.getMessage());
        }
      }
    }


    RestApiAdaptador restApiAdaptador = new RestApiAdaptador();
    EndPointsApi endPointsApi = restApiAdaptador.conexionRestAPI();

    Post post = new Post();
    post.setCodigo(codigo);

    Call<Post> call = endPointsApi.registrarAcceso(post);
    call.enqueue(new Callback<Post>() {
      @Override
      public void onResponse(Call<Post> call, Response<Post> response) {
        if (response.body() != null) {
          Log.e("JSON RESULT", "" + response.body().toString());
          mostrarRegistro(response.body());
        } else {
          Toast.makeText(getBaseContext(), "Empleado no encontrado", Toast.LENGTH_LONG).show();
        }
      }

      @Override
      public void onFailure(Call<Post> call, Throwable t) {
        t.printStackTrace();
        Log.e("FALLO LA CONEXION", t.toString());
        Toast.makeText(getBaseContext(), "The requested URL was not found on this server.", Toast.LENGTH_SHORT).show();
      }
    });
  }

  public void mostrarRegistro(Post respuestaRegistro) {
    ivFotoEmpresa.setVisibility(View.VISIBLE);
    ivFotoLove.setVisibility(View.VISIBLE);
    LocalDateTime localDateTime = LocalDateTime.now();
    //respuestaRegistro = new Post();
    // respuestaRegistro.setNombre("YESCAS SANCHEZ SALVADOR");
    // respuestaRegistro.setHora(localDateTime.getHour() + ":" + localDateTime.getMinute() + ":" + localDateTime.getSecond());
    //respuestaRegistro.setFecha("2024-01-11");
    //respuestaRegistro.setEmpleado("29");
    //respuestaRegistro.setFoto("");//https://200.92.206.26:4433/gazsbm/public/api/empleado/imagen/29

    tvNombre.setText(respuestaRegistro.getNombre());
    tvHora.setText(respuestaRegistro.getHora());
    // tvFecha.setText(respuestaRegistro.getFecha());
    if (respuestaRegistro.getEmpleado().equals("9999")) {
      tvNombre.setTextColor(Color.RED);
      ivFoto.setImageResource(R.drawable.gota_blanca);
    } else {
      //Picasso.get().load(respuestaRegistro.getFoto()).fit().into(ivFoto);
      if (respuestaRegistro.getFoto().isEmpty()) {
        // obtener el logo de la empresa o no se encontro
        //   Picasso.get().load(R.drawable.imagenotfound).fit().into(ivFoto);
        Glide.with(getBaseContext()).load(R.drawable.imagenotfound).apply(RequestOptions.circleCropTransform()).into(ivFoto);
      } else {
        //Picasso.get().load(respuestaRegistro.getFoto()).fit().into(ivFoto);
        Glide.with(getBaseContext()).load(respuestaRegistro.getFoto()).apply(RequestOptions.circleCropTransform()).into(ivFoto);
      }
      //mostrar foto de la empresa
      if (!sCompany.isEmpty()) {
        Context context = ivFotoEmpresa.getContext();
        int id = context.getResources().getIdentifier("logo_" + sCompany, "drawable", context.getPackageName());
        ivFotoEmpresa.setImageResource(id);
      } else {
        ivFotoEmpresa.setImageResource(R.drawable.imagenotfound);
      }
    }

    txtBienvenida.setText("¡Buen día!");
    //  Glide.with(getBaseContext()).load(R.drawable.foto1).apply(RequestOptions.circleCropTransform()).into(ivFoto);
  }

  public void refreshInterface() {
    etCodigo.setText("");
    etCodigo.requestFocus();
    // lyDetalleAcceso.setVisibility(View.INVISIBLE);
  }

  @Override
  public void onClick(View view) {

        /*if(view.getId() == btnEscanear.getId()){
            IntentIntegrator intent = new IntentIntegrator(this);
            intent.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);

            intent.setPrompt("Scan");
            intent.setCameraId(0);
            intent.setBeepEnabled(true);
            intent.setBarcodeImageEnabled(false);
            intent.initiateScan();
        }*/
  }

  @Override
  public void onBackPressed() {
    //if(lyDetalleAcceso.getVisibility() == View.INVISIBLE) {
    AlertDialog.Builder alerta = new AlertDialog.Builder(this);
    alerta.setTitle("ESTA SEGURO QUE DESEA SALIR?");
    alerta.setPositiveButton("SALIR", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        android.os.Process.killProcess(android.os.Process.myPid()); //Su funcion es algo similar a lo que se llama cuando se presiona el botón "Forzar Detención" o "Administrar aplicaciones", lo cuál mata la aplicación
        finishAffinity(); //Si solo quiere mandar la aplicación a segundo plano
        System.exit(0);
      }
    });
    alerta.setNegativeButton("PERMANECER", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        etCodigo.setError(null);
      }
    });
    alerta.show();
        /*}
        else {
            refreshInterface();
        }*/

  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
    if (result != null) {
      if (result.getContents() == null) {
        Toast.makeText(this, "ESCANEO CANCELADO", Toast.LENGTH_LONG).show();
      } else {
        String employeeAndCompany = result.getContents();
        String employeeNumber = result.getContents();
        if (employeeNumber.length() == 3) {
          configurarEmpresa(employeeNumber);
          return;
        } else if (employeeNumber.length() == 4) {
          String empresa = obtenerEmpresa();
          if (empresa != null) {
            employeeAndCompany = empresa + employeeNumber;
          } else {
            Toast.makeText(getBaseContext(), "La Empresa no esta configurada", Toast.LENGTH_LONG).show();
          }
        } else if (employeeNumber.length() == 7) {
          employeeNumber = employeeNumber.substring(3, 7);
        }

        etCodigo.setText(employeeNumber);
        registrarAccesoWS(employeeAndCompany);
        // lyDetalleAcceso.setVisibility(View.VISIBLE);
      }
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

  private String obtenerEmpresa() {
    appDatabase = Room
            .databaseBuilder(this, AppDatabase.class, "dbControlAcceso")
            .allowMainThreadQueries()
            .build();

    SysConfig config = appDatabase.sysDao().findByCode("CCOMP");
    if (config != null) {
      return config.getValue();
    }
    return null;
  }

  private void configurarEmpresa(String employeeNumber) {
    appDatabase = Room
            .databaseBuilder(this, AppDatabase.class, "dbControlAcceso")
            .allowMainThreadQueries()
            .build();

    SysConfig config = appDatabase.sysDao().findByCode("CCOMP");
    sCompany = employeeNumber;
    if (config == null) {
      config = new SysConfig();
      config.setCode("CCOMP");
      config.setName("Empresa actual");
      config.setValue(employeeNumber);
      appDatabase.sysDao().insert(config);
      showMessage();
    } else {
      config.setValue(employeeNumber);
      appDatabase.sysDao().update(config);
      showMessage();
    }
  }

  private void showMessage() {
    dialog.setContentView(R.layout.win_layout_dialog);
    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    TextView txtCompanyCode = dialog.findViewById(R.id.txtCompanyCode);
    txtCompanyCode.setText(sCompany);

    ImageView imageViewComany = dialog.findViewById(R.id.imageViewComany);
    Context context = imageViewComany.getContext();
    int id = context.getResources().getIdentifier("logo_" + sCompany, "drawable", context.getPackageName());
    imageViewComany.setImageResource(id);

    Button button = dialog.findViewById(R.id.btnAccept);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dialog.dismiss();
      }
    });

    dialog.show();
  }

  @Override
  protected void onResume() {
    super.onResume();

    barcodeView.resume();
  }

  @Override
  protected void onPause() {
    super.onPause();

    barcodeView.pause();
  }

  public void pause(View view) {
    barcodeView.pause();
  }

  public void resume(View view) {
    barcodeView.resume();
  }

  public void triggerScan(View view) {
    barcodeView.decodeSingle(callback);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);

  }
}