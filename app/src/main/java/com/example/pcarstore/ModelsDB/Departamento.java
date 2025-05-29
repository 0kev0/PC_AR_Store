package com.example.pcarstore.ModelsDB;

import java.util.ArrayList;
import java.util.List;

public class Departamento {
    /*************************************************************VARIABLES******************************************************************************************/
    private String id;
    private String nombre;
    private String ciudadSede;
    private List<String> ciudades;
    private double costoBaseEnvio;
    private double costoEnvioSede;

    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public String getNombre() {
        return nombre;
    }


    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCiudadSede() {
        return ciudadSede;
    }

    public void setCiudadSede(String ciudadSede) {
        this.ciudadSede = ciudadSede;
    }

    public List<String> getCiudades() {
        return ciudades;
    }

    public void setCiudades(List<String> ciudades) {
        this.ciudades = ciudades;
    }

    public double getCostoBaseEnvio() {
        return costoBaseEnvio;
    }

    public void setCostoBaseEnvio(double costoBaseEnvio) {
        this.costoBaseEnvio = costoBaseEnvio;
    }

    public double getCostoEnvioSede() {
        return costoEnvioSede;
    }

    public void setCostoEnvioSede(double costoEnvioSede) {
        this.costoEnvioSede = costoEnvioSede;
    }

    // Metodo para calcular costo de envío
    public double calcularCostoEnvio(String ciudadDestino) {
        if (ciudadDestino == null || ciudadDestino.isEmpty()) {
            return costoBaseEnvio; // Costo por defecto
        }

        if (ciudadDestino.equals(ciudadSede)) {
            return costoEnvioSede; // Envío dentro de la ciudad sede
        }

        if (ciudades.contains(ciudadDestino)) {
            return costoBaseEnvio; // Envío a otras ciudades del departamento
        }

        return costoBaseEnvio * 1.5; // Costo aumentado para ciudades fuera del departamento
    }


    public static class Builder {
        private String nombre;
        private String ciudadSede;
        private List<String> ciudades = new ArrayList<>();
        private double costoBaseEnvio = 5.99;
        private double costoEnvioSede = 2.99;

        public Builder(String nombre) {
            this.nombre = nombre;
        }

        public Builder establecerCiudadSede(String ciudadSede) {
            this.ciudadSede = ciudadSede;
            return this;
        }

        public Builder agregarCiudad(String ciudad) {
            if (!ciudades.contains(ciudad)) {
                this.ciudades.add(ciudad);
            }
            return this;
        }

        public Builder conCostoBaseEnvio(double costo) {
            this.costoBaseEnvio = costo;
            return this;
        }

        public Builder conCostoEnvioSede(double costo) {
            this.costoEnvioSede = costo;
            return this;
        }

        public Departamento build() {
            Departamento dep = new Departamento();
            dep.setNombre(this.nombre);
            dep.setCiudadSede(this.ciudadSede);
            dep.setCiudades(this.ciudades);
            dep.setCostoBaseEnvio(this.costoBaseEnvio);
            dep.setCostoEnvioSede(this.costoEnvioSede);

            // Asegurarse que la ciudad sede esté en la lista de ciudades
            if (this.ciudadSede != null && !this.ciudades.contains(this.ciudadSede)) {
                this.ciudades.add(this.ciudadSede);
            }

            return dep;
        }
    }

}