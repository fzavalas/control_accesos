package com.lechelaimperial.controlaccesos.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Post {

    @SerializedName("Empleado")
    @Expose
    private String empleado;
    @SerializedName("Nombre")
    @Expose
    private String nombre;
    @SerializedName("Foto")
    @Expose
    private String foto;
    @SerializedName("Fecha")
    @Expose
    private String fecha;
    @SerializedName("Hora")
    @Expose
    private String hora;

    @SerializedName("codigo")
    @Expose
    private String codigo;

    public String getEmpleado() {
        return empleado;
    }

    public void setEmpleado(String empleado) {
        this.empleado = empleado;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}