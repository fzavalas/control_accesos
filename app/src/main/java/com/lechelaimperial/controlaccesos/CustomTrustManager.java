package com.lechelaimperial.controlaccesos;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

public class CustomTrustManager implements X509TrustManager {
  @Override
  public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    // No realizamos ninguna acción especial en la verificación del cliente.
  }

  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    // Aquí es donde puedes personalizar la validación del certificado del servidor.
    // Por ejemplo, puedes implementar tu propia lógica de validación.

    // Ejemplo simple: Aceptar todos los certificados (NO RECOMENDADO para producción).
    for (X509Certificate cert : chain) {
      cert.checkValidity(); // Esto verifica la validez del certificado.
    }
  }

  @Override
  public X509Certificate[] getAcceptedIssuers() {
    // Devuelve un array de certificados emisores que son aceptados para la autenticación de servidores.
    return new X509Certificate[0];
  }
}