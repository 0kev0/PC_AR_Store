package com.example.pcarstore.Services;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.example.pcarstore.ModelsDB.Category;
import com.example.pcarstore.ModelsDB.Departamento;
import com.example.pcarstore.ModelsDB.DiscountCode;
import com.example.pcarstore.ModelsDB.GiftCard;
import com.example.pcarstore.ModelsDB.Product;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

/**
 * Service class para inicializar database
 */
public class DatabaseSeederService {

    private final DatabaseReference mDatabase;

    public DatabaseSeederService() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Initialize all database data
     */
    public void initializeDatabase() {
        insertCategories();
        //insertDepartamentos();
        insertProducts();
        insertGiftCards();
        insertDiscountCodes();
    }

    /**
     * Insert sample PC products into the database
     */
    private void insertProducts() {
        Product product1 = new Product(
                "prod_001",
                "NVIDIA RTX 4090",
                1999.99,
                1799.99,
                15,
                4.9,
                "Tarjetas Gráficas",
                "La GPU más potente para gaming 8K y creación de contenido con arquitectura Ada Lovelace",
                Arrays.asList("https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_002%2FIMG%2Fnvidia-rtx-4090-2835295.webp?alt=media&token=2dfe5c06-ef4e-4fc9-ac3e-ccd8d67dadba",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_002%2FIMG%2FDSC_0249-Custom-1480x987.jpg?alt=media&token=b46d4334-67e2-4e73-85b0-42b069c5165c",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_002%2FIMG%2Frtx_nobg.jpg?alt=media&token=d2add133-2592-45ae-b78a-297d3bc44c0d"),
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_003%2FMODELO_3D%2FUntitled.obj?alt=media&token=3e39b974-9e49-4a65-8f91-b8d6070b271d",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_003%2FTEXTURA%2FtexturaGenerica.jpg?alt=media&token=7a78ad20-eff2-4d48-a942-47ecc0ec0c83",
                new HashMap<String, String>() {{
                    put("Arquitectura", "Ada Lovelace");
                    put("Núcleos CUDA", "16384");
                    put("Memoria GDDR6X", "24GB");
                    put("Velocidad Memoria", "21 Gbps");
                    put("Ancho de banda", "1 TB/s");
                    put("TDP", "450W");
                    put("Conectores", "PCIe 4.0 x16");
                    put("Salidas", "3x DisplayPort 1.4a, 1x HDMI 2.1a");
                }}
        );
        mDatabase.child("products").child("prod_001").setValue(product1);
        // Procesadores
        Product product2 = new Product(
                "prod_002",
                "Intel Core i9-13900K",
                599.99,
                549.99,
                30,
                4.9,
                "Procesadores",
                "Procesador de 24 núcleos (8P+16E) y 32 hilos con frecuencia turbo de hasta 5.8GHz",
                Arrays.asList("https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_001%2FIMG%2Fimg1.jpg?alt=media&token=9c1be58c-ac08-4906-95f9-cb1e91e41ce4",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_001%2FIMG%2Fimg2.jpeg?alt=media&token=807e0ae0-53cf-43bd-9b1f-fae03aaec77f",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_001%2FIMG%2Fimg3.webp?alt=media&token=5dce4891-42b9-4f24-9b6e-bd3627dd6ac0"),
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_001%2FMODELO_3D%2FintelCore9Model.obj?alt=media&token=536f91fd-085d-4e6d-ad72-0d61ff00d662",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_001%2FTEXTURAS%2Ftexturametalica.jpeg?alt=media&token=81d45962-8954-4e66-ae02-190ceebfca5f",
                new HashMap<String, String>() {{
                    put("Núcleos", "24 (8P+16E)");
                    put("Hilos", "32");
                    put("Frecuencia base", "3.0 GHz");
                    put("Frecuencia turbo", "5.8 GHz");
                    put("Socket", "LGA 1700");
                    put("TDP", "125W");
                    put("Caché", "36MB");
                }}
        );
        mDatabase.child("products").child("prod_002").setValue(product2);

        Product product3 = new Product(
                "prod_003",
                "no hay Intel Core i5-13600KF",
                299.99,
                279.99,
                40,
                4.7,
                "Procesadores",
                "Procesador de 14 núcleos (6P+8E) sin gráficos integrados para gaming",
                Arrays.asList("", ""),
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_001%2FMODELO_3D%2FintelCore9Model.obj?alt=media&token=536f91fd-085d-4e6d-ad72-0d61ff00d662",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_001%2FTEXTURAS%2Ftexturametalica.jpeg?alt=media&token=81d45962-8954-4e66-ae02-190ceebfca5f",
                new HashMap<String, String>() {{
                    put("Núcleos", "14 (6P+8E)");
                    put("Hilos", "20");
                    put("Frecuencia base", "3.5 GHz");
                    put("Frecuencia turbo", "5.1 GHz");
                    put("Socket", "LGA 1700");
                    put("TDP", "125W");
                    put("Caché", "24MB");
                }}
        );
        mDatabase.child("products").child("prod_003").setValue(product3);

        // Tarjetas Gráficas
        Product product4 = new Product(
                "prod_004",
                "Teclado Mecánico Gaming RGB",
                159.99,
                139.99,
                13,
                4.7,
                "Periféricos",
                "Teclado mecánico gaming con interruptores táctiles, retroiluminación RGB personalizable y construcción duradera",
                Arrays.asList("https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_004%2FIMG%2F614U66QoZQL._AC_SL1500_.jpg?alt=media&token=d0ba0361-123e-49a4-90aa-a218da4ac51b",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_004%2FIMG%2F71Nny-l8oRL._AC_SL1500_.jpg?alt=media&token=bdc91f9d-8e58-4daa-b48f-93837faa1ff0",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_004%2FIMG%2F71rZuZSa4yL._AC_SL1500_.jpg?alt=media&token=f4082b39-2367-4c2f-b132-454da665f4ab"),
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_004%2FMODELO_3D%2FUntiteclado.obj?alt=media&token=f5d051ec-a007-42bf-8681-921794b215f6+",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_004%2FTEXTURAS%2FtexturaGenerica.jpg?alt=media&token=8648eba1-a65e-4833-b529-75f6c40ecc83",
                new HashMap<String, String>() {{
                    put("Tipo", "Mecánico");
                    put("Interruptores", "Red switches (lineales)");
                    put("Retroiluminación", "RGB 16.8 millones de colores");
                    put("Teclas", "104 teclas anti-ghosting");
                    put("Material", "ABS doble inyección");
                    put("Conexión", "USB 2.0 gold-plated");
                    put("Tasa de sondeo", "1000Hz");
                    put("Dimensiones", "440 x 135 x 38 mm");
                    put("Peso", "1.1 kg");
                }}
        );
        mDatabase.child("products").child("prod_004").setValue(product4);

        Product product5 = new Product(
                "prod_005",
                "Combo Gaming Teclado + Mouse RGB",
                129.99,
                109.99,
                15,
                4.7,
                "Periféricos",
                "Kit completo para gaming con teclado mecánico y mouse ergonómico, iluminación RGB sincronizada",
                Arrays.asList("https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_005%2FIMG%2F61Cks99XBwL._AC_SL1500_.jpg?alt=media&token=07fc6cb8-21c3-4f4c-8a23-2e32e4e1c2d3",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_005%2FIMG%2F61bhDUxAB0L._AC_SL1500_.jpg?alt=media&token=33932c9c-404b-4f70-b8c2-ddc2cc6b59e2",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_005%2FIMG%2F71T5d2WerYL._AC_SL1500_.jpg?alt=media&token=5ef4a4a9-cfa7-4a64-b688-5f270fb5b63c",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_005%2FIMG%2F71wc5rhfSdL._AC_SL1500_.jpg?alt=media&token=ece7f335-f459-47e7-ad8a-e28ac267dfb7"),
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_005%2FMODELO_3D%2FUntitled.obj?alt=media&token=76b62072-bb29-4c7c-aa13-efd7f3587191",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_005%2FTEXTURAS%2FMouse_baseColor.png?alt=media&token=d99833da-38f3-40af-aa09-7482a7547ead",
                new HashMap<String, String>() {{
                    put("Teclado - Tipo", "Mecánico con switches Red");
                    put("Teclado - Teclas", "104 teclas anti-ghosting");
                    put("Teclado - Iluminación", "RGB 16.8M colores");
                    put("Mouse - Sensor", "Óptico 12,000 DPI");
                    put("Mouse - Botones", "6 programables");
                    put("Mouse - Peso", "110g (ajustable)");
                    put("Conexión", "USB 2.0");
                    put("Compatibilidad", "Windows, macOS, Linux");
                    put("Incluye", "Reposamuelles, cable trenzado");
                }}
        );
        mDatabase.child("products").child("prod_005").setValue(product5);

        Product product6 = new Product(
                "prod_006",
                "Auriculares Premium con Cancelación de Ruido",
                299.99,
                249.99,
                17,
                4.8,
                "Audio",
                "Auriculares inalámbricos con cancelación activa de ruido (ANC) y sonido Hi-Res para una experiencia inmersiva",
                Arrays.asList("https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_006%2FIMG%2F51rpbVmi9XL._AC_SL1200_.jpg?alt=media&token=b66f3d86-faeb-41b6-9bd5-f02b13e84126",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_006%2FIMG%2F71hQC0mJ9BL._AC_SL1500_.jpg?alt=media&token=b44fdbec-955c-4376-b4d2-e5a1ad6ccb0c",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_006%2FIMG%2F41co0OjQB%2BL._AC_SL1200_.jpg?alt=media&token=501d049c-29f7-4898-a470-af1b9d5b7087",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_006%2FIMG%2F91hx2gnY0qL._AC_SL1500_.jpg?alt=media&token=e895a1a4-1bf5-48e0-9f19-6d0dff8d3b67"),
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_006%2FMODELO_3D%2FheadphonesSF.obj?alt=media&token=81dad9c7-c9e3-4be0-a6b1-ebe0768efd9b",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_006%2FTEXTURAS%2FHeadphones_Base_Color.png?alt=media&token=f8c6aa33-2d88-4bd9-8fd5-8a68b6669887",
                new HashMap<String, String>() {{
                    put("Tipo", "Over-ear inalámbricos");
                    put("Cancelación de ruido", "ANC con 3 modos");
                    put("Autonomía", "30 horas (con ANC)");
                    put("Carga rápida", "5 min = 3 horas de uso");
                    put("Control de sonido", "Equalizador personalizable");
                    put("Conectividad", "Bluetooth 5.2 + entrada 3.5mm");
                    put("Micrófono", "Array de 4 mics con reducción de ruido");
                    put("Respuesta en frecuencia", "4Hz-40kHz");
                    put("Peso", "265g");
                    put("Incluye", "Estuche rígido, cable USB-C y audio");
                }}
        );
        mDatabase.child("products").child("prod_006").setValue(product6);

        // Memoria RAM
        Product product7 = new Product(
                "prod_007",
                "Kit RAM HyperX Fury DDR4 32GB",
                149.99,
                129.99,
                13,
                4.7,
                "Memoria RAM",
                "Memoria HyperX Fury DDR4 con disipador de calor y perfil bajo para compatibilidad mejorada",
                Arrays.asList("https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_007%2FIMG%2F61uXihcspuL._AC_SL1500_.jpg?alt=media&token=3745f8e4-a54f-4c9e-a79b-9ee81306443c",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_007%2FIMG%2F61MYgWr%2BxWL._AC_SL1500_.jpg?alt=media&token=f3863210-f1f6-4061-810d-a49d0d242972",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_007%2FIMG%2F61kfY6Ydm2L._AC_SL1200_.jpg?alt=media&token=d959d8e5-39af-47f1-8263-101f70d8de69",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_007%2FIMG%2F71hj9eGW4ML._AC_SL1500_.jpg?alt=media&token=c3fcc199-d73a-4e35-9d8b-83798d95bced"),
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_007%2FMODELO_3D%2FUntitled.obj?alt=media&token=c3f489ea-49b6-442d-a3c0-05e0330cb307",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_007%2FTEXTURAS%2FtexturaGenerica.jpg?alt=media&token=afc6e879-6985-49fc-a3ae-5b378772ca4a",
                new HashMap<String, String>() {{
                    put("Capacidad", "32GB (2x16GB)");
                    put("Tecnología", "DDR4");
                    put("Velocidad", "3200MHz");
                    put("Latencia", "CL16");
                    put("Voltaje", "1.35V");
                    put("Compatibilidad", "Intel XMP-ready");
                    put("Disipador", "Heat spreader negro");
                    put("Perfil", "Bajo (34.1mm)");
                    put("Garantía", "Vitalicia limitada");
                }}
        );
        mDatabase.child("products").child("prod_007").setValue(product7);

        Product product8 = new Product(
                "prod_008",
                "USB Kingston DataTraveler Max 1TB",
                129.99,
                109.99,
                15,
                4.6,
                "Almacenamiento",
                "USB 3.2 Gen 2 de alta velocidad con diseño compacto y resistente",
                Arrays.asList("https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_008%2FIMG%2FUSB%20Kingston%20DataTraveler%20Max%201TB%201.jpg?alt=media&token=3314ab78-345b-4f65-bfa6-b88a80335522",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_008%2FIMG%2FUSB%20Kingston%20DataTraveler%20Max%201TB%202.jpg?alt=media&token=ea1e5cdb-1f13-4fb2-8f45-bd781775311b",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_008%2FIMG%2FUSB%20Kingston%20DataTraveler%20Max%201TB%203.jpg?alt=media&token=7d3ec94e-0100-4bb2-88b8-b3bb07ec41ab",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_008%2FIMG%2FUSB%20Kingston%20DataTraveler%20Max%201TB%204.jpg?alt=media&token=47e43d28-6cb0-470e-88a7-e0f7bcb9d9f6"),
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_008%2FMODELO_3D%2FUntitled.obj?alt=media&token=183ab3a4-7b64-4812-9d65-d69a7df5fa63",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_008%2FTEXTURAS%2FtexturaGenerica.jpg?alt=media&token=5fa7e17d-0c99-4ef6-8ca9-1646cfaa5fba ",
                new HashMap<String, String>() {{
                    put("Capacidad", "1TB");
                    put("Interfaz", "USB 3.2 Gen 2");
                    put("Velocidad lectura", "1000MB/s");
                    put("Velocidad escritura", "900MB/s");
                    put("Compatibilidad", "USB-A y USB-C incluido");
                    put("Seguridad", "Cifrado AES de 256-bit");
                    put("Resistencia", "A prueba de agua y polvo (IP55)");
                    put("Dimensiones", "72.94 x 26.92 x 12.5 mm");
                    put("Garantía", "5 años");
                }}
        );
        mDatabase.child("products").child("prod_008").setValue(product8);

        Product product9 = new Product(
                "prod_009",
                "Microsoft Surface Laptop 5",
                1299.99,
                1199.99,
                8,
                4.8,
                "Laptops",
                "Laptop premium con pantalla táctil PixelSense, procesador Intel Core i7 y diseño ultradelgado",
                Arrays.asList("https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_009%2FIMG%2FMicrosoft%20Surface%20Laptop%205%201.webp?alt=media&token=fefd598f-df19-4f8f-b9c1-8eefe7fda3aa",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_009%2FIMG%2FMicrosoft%20Surface%20Laptop%205%202.webp?alt=media&token=d218e7e1-1d8e-48a0-8168-b5d5db8ab6cf",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_009%2FIMG%2FMicrosoft%20Surface%20Laptop%205%204.jpg?alt=media&token=bcffebd9-663c-4565-bb90-0e4bd2ccdd14",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_009%2FIMG%2FMicrosoft%20Surface%20Laptop%205%203.png?alt=media&token=762466df-b231-4e08-b5b5-ee107edfb478"),
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_009%2FMODELOS_3D%2FUntitled.obj?alt=media&token=180a76d7-3015-4186-821d-2a361b8fba18",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_009%2FTEXTURAS%2FLaptop1_baseColor.png?alt=media&token=de697db7-ca04-487a-bc02-7471812a27f9",
                new HashMap<String, String>() {{
                    put("Procesador", "Intel Core i7-1255U (10 núcleos)");
                    put("Memoria RAM", "16GB LPDDR5X");
                    put("Almacenamiento", "512GB SSD");
                    put("Pantalla", "13.5\" PixelSense (2256 x 1504) táctil");
                    put("Grágicos", "Intel Iris Xe");
                    put("Sistema Operativo", "Windows 11 Pro");
                    put("Batería", "Hasta 18 horas de uso");
                    put("Conectividad", "WiFi 6, Bluetooth 5.1");
                    put("Puertos", "USB-C, USB-A, Surface Connect, 3.5mm");
                    put("Peso", "1.27 kg");
                    put("Material", "Aluminio y Alcantara");
                }}
        );
        mDatabase.child("products").child("prod_009").setValue(product9);

        // Almacenamiento
        Product product10 = new Product(
                "prod_010",
                "SSD Kingston A400 960GB SATA III",
                89.99,
                79.99,
                11,
                4.7,
                "Almacenamiento",
                "Disco sólido SATA III de alto rendimiento para actualización de equipos",
                Arrays.asList("https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_010%2FIMG%2FSD%20Kingston%20A400%20960GB%20SATA%20III%201.jpg?alt=media&token=fb6c0795-c6d2-4d15-b6ad-aa6929d0658d",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_010%2FIMG%2FSD%20Kingston%20A400%20960GB%20SATA%20III%202.webp?alt=media&token=96803ffe-9d87-49c6-a96e-97b9c495177f",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_010%2FIMG%2FSD%20Kingston%20A400%20960GB%20SATA%20III%203.jpg?alt=media&token=9fb87848-669c-48fd-9dd8-c85d6d5ddd09",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_010%2FIMG%2FSD%20Kingston%20A400%20960GB%20SATA%20III%204.jpg?alt=media&token=c9e18c7b-d055-46a0-908d-6efc968cb6de"),
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_010%2FMOELDO_3D%2FUntitled.obj?alt=media&token=851e5ce9-803b-4a6a-bfb4-5763f7e30256",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_010%2FTEXTURAS%2FNone_baseColor.png?alt=media&token=13ced338-472e-4cfb-836a-0cbdb33c3069",
                new HashMap<String, String>() {{
                    put("Capacidad", "960GB");
                    put("Interfaz", "SATA III 6Gb/s");
                    put("Lectura", "500 MB/s");
                    put("Escritura", "450 MB/s");
                    put("Factor de forma", "2.5\"");
                    put("Consumo", "Bajo (1.4W idle)");
                    put("Resistencia", "1500G anti-shock");
                    put("Tecnología", "3D NAND");
                    put("Garantía", "3 años");
                    put("Compatibilidad", "PCs y laptops");
                }}
        );
        mDatabase.child("products").child("prod_010").setValue(product10);

        Product product11 = new Product(
                "prod_011",
                "Peluche de Gato Realista 40cm",
                29.99,
                24.99,
                17,
                4.9,
                "Juguetes",
                "Peluche suave y realista de gato, perfecto para niños y coleccionistas",
                Arrays.asList("https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_011%2FIMG%2FPeluche%20de%20Gato%20Realista%2040cm%201.jpg?alt=media&token=5044160a-0d17-4cf8-8539-5b0172958fee",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_011%2FIMG%2FPeluche%20de%20Gato%20Realista%2040cm%202.webp?alt=media&token=2d1c5831-a329-4f4d-8b0c-5bd6b3bdda6e",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_011%2FIMG%2FPeluche%20de%20Gato%20Realista%2040cm%203.webp?alt=media&token=7a4190cc-5640-48bc-b81c-d6468ece9ec0",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_011%2FIMG%2FPeluche%20de%20Gato%20Realista%2040cm%204.jpg?alt=media&token=0ef82045-fe5a-4989-891b-23244c701253"),
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_011%2FMODELOS_3D%2FUntitled.obj?alt=media&token=a77545d1-8c31-408b-b81d-53f97a534907",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_011%2FTEXTURAS%2FMaterial.001_baseColor.png?alt=media&token=467f39c8-2f1c-4333-8026-5654215cca90",
                new HashMap<String, String>() {{
                    put("Altura", "40 cm");
                    put("Material", "Poliéster hipoalergénico");
                    put("Relleno", "Fibra siliconada ultra suave");
                    put("Colores disponibles", "Atigrado, negro, blanco, naranja");
                    put("Edad recomendada", "3+ años");
                    put("Lavable", "Sí (superficie con paño húmedo)");
                    put("Características especiales", "Bigotes de nylon resistente");
                    put("Incluye", "Collar con cascabel decorativo");
                    put("Certificaciones", "Normas CE de seguridad");
                }}
        );
        mDatabase.child("products").child("prod_011").setValue(product11);

        Product product12 = new Product(
                "prod_012",
                "SSD WD Black SN850X 1TB NVMe M.2",
                129.99,
                109.99,
                15,
                4.9,
                "Almacenamiento",
                "SSD NVMe PCIe Gen4 de alto rendimiento para gaming y creación de contenido",
                Arrays.asList("https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_012%2FIMG%2FSSD%20WD%20Black%20SN850X%201TB%20NVMe%20M.2%201.webp?alt=media&token=c4e307e7-0311-4523-b297-a992f3814893",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_012%2FIMG%2FSSD%20WD%20Black%20SN850X%201TB%20NVMe%20M.2%202.avif?alt=media&token=eb7f89c0-1cb5-445a-8567-f6b398c52e46",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_012%2FIMG%2FSSD%20WD%20Black%20SN850X%201TB%20NVMe%20M.2%203.jpg?alt=media&token=6585b8eb-8e4a-4d97-872b-2413bb7847d2",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_012%2FIMG%2FSSD%20WD%20Black%20SN850X%201TB%20NVMe%20M.2%204.jpg?alt=media&token=1443ba9d-91b5-4df8-8581-054b6d4a001c"),
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_012%2FMODELOS_3D%2FSSD_M2.obj?alt=media&token=e5df14e3-35b7-4f76-8aa8-114a323e57f0",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_012%2FTEXTURAS%2Fne.png?alt=media&token=de70d137-caa2-48c9-893d-24f8507032a4",
                new HashMap<String, String>() {{
                    put("Capacidad", "1TB");
                    put("Interfaz", "PCIe Gen4 x4");
                    put("Velocidad Lectura", "7300 MB/s");
                    put("Velocidad Escritura", "6300 MB/s");
                    put("Factor de forma", "M.2 2280");
                    put("Tecnología", "3D NAND");
                    put("TBW", "600 TB");
                    put("MTTF", "1.75 millones de horas");
                    put("Garantía", "5 años");
                    put("Incluye", "Software WD Black Dashboard");
                }}
        );
        mDatabase.child("products").child("prod_012").setValue(product12);

        // Placas Base
        Product product13 = new Product(
                "prod_013",
                "Alexa echo III",
                599.99,
                549.99,
                15,
                4.9,
                "Asistentes de voz",
                "Asistente de voz inteligente de última generación con conectividad mejorada",
                Arrays.asList("https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_013%2FIMG%2FOIP.jpeg?alt=media&token=5059136b-2857-41da-b886-9b5f7bf04c4b",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_013%2FIMG%2F01LW4140443_HeroSquare-525fa698ae274ad0a534f39ae1acbc2d.jpg?alt=media&token=23aa3fc3-5f4d-43e8-8a06-1c43bf971568",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_013%2FIMG%2FR.jpeg?alt=media&token=90fb3d9e-4023-4099-86f3-e3a80eb81e83",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_013%2FIMG%2Fv8RBJYunWkui5xbKQiYGpe.jpg?alt=media&token=f9938e4a-06be-470c-8a1b-a0a859a24d4a"),
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_013%2FMODELOS_3D%2FUntitled.obj?alt=media&token=f00ecc75-5289-4411-879e-9880496d3c2e",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_013%2FTEXTURAS%2Falexa_texture_0.png?alt=media&token=c733edea-ddb1-453c-aeb5-133f95fde63b",
                new HashMap<String, String>() {{
                    put("Tecnología de voz", "Reconocimiento avanzado");
                    put("Conectividad", "WiFi 6E, Bluetooth 5.2");
                    put("Altavoces", "Sistema de audio premium 360°");
                    put("Procesador", "CPU multi-núcleo dedicada");
                    put("Compatibilidad", "Funciona con dispositivos inteligentes principales");
                    put("Asistente", "Alexa con IA mejorada");
                    put("Pantalla", "Pantalla táctil HD integrada");
                }}
        );
        mDatabase.child("products").child("prod_013").setValue(product13);

        Product product14 = new Product(
                "prod_014",
                "Mouse logitec",
                219.99,
                199.99,
                25,
                4.7,
                "Placas Base",
                "Placa base AMD AM5 con sólida VRM y conectividad",
                Arrays.asList("https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_014%2FIMG%2FOIP%20(36).jpg?alt=media&token=fc7c276d-2fcf-480f-bdf1-88ab112fbf65",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_014%2FIMG%2FOIP%20(34).jpg?alt=media&token=d16dd7e6-2809-4900-8118-24bdda3a837d",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_014%2FIMG%2FOIP%20(35).jpg?alt=media&token=b82e6c98-493d-4c6e-a8e4-d30ef576c98f",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_014%2FIMG%2FOIP%20(35).jpg?alt=media&token=b82e6c98-493d-4c6e-a8e4-d30ef576c98f"),
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_014%2FMODELOS_3D%2FWireless_Simple_Mouse_0301141012_texture.obj?alt=media&token=39c5fb8d-1d28-4522-880c-789fe82fab2c",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_014%2FTEXTURAS%2FWireless_Simple_Mouse_0301141012_texture.png?alt=media&token=eadc04ce-76e1-47ea-9ec4-0160b5541469",
                new HashMap<String, String>() {{
                    put("Chipset", "AMD B650");
                    put("Socket", "AM5");
                    put("Formato", "ATX");
                    put("Memoria", "4x DDR5 hasta 128GB");
                    put("PCIe", "1x PCIe 4.0 x16");
                    put("M_2", "2x slots M_2");
                    put("Red", "2.5Gb Ethernet");
                }}
        );
        mDatabase.child("products").child("prod_014").setValue(product14);

        Product product15 = new Product(
                "prod_015",
                "Arduino uno ",
                499.99,
                469.99,
                10,
                4.8,
                "Placas Base",
                "Placa base flagship para Ryzen 7000 con PCIe 5.0 completo",
                Arrays.asList("https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_015%2FIMG%2FOIP%20(37).jpg?alt=media&token=292f6753-e891-41f7-9fbc-b63e47f52200",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_015%2FIMG%2FOIP%20(38).jpg?alt=media&token=b37e2cb5-4193-4928-9b33-ab2fc82cb7ee",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_015%2FIMG%2FOIP%20(39).jpg?alt=media&token=64f3d99b-f0a2-4a40-b114-8fe301aa55dc",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_015%2FIMG%2Farduino-uno-rev3.jpg?alt=media&token=68ef8887-f31d-45c3-b6ce-8023af002c55"),
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_015%2FMODELO_3D%2FarduinoUno.obj?alt=media&token=d971c829-681f-4f54-aad3-98e05fda951c",
    "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_015%2FTEXTURA%2FtexturaArduino.jpg?alt=media&token=be5aeb61-4bc9-4bb8-8fbd-e293d3ad5fab",
                new HashMap<String, String>() {{
                    put("Chipset", "AMD X670E");
                    put("Socket", "AM5");
                    put("Formato", "E-ATX");
                    put("Memoria", "4x DDR5 hasta 128GB");
                    put("PCIe", "2x PCIe 5.0 x16");
                    put("M_2", "4x slots M_2 (PCIe 5.0)");
                    put("Red", "WiFi 6E, 10Gb Ethernet");
                }}
        );
        mDatabase.child("products").child("prod_015").setValue(product15);

        // Fuentes de Alimentación
        Product product16 = new Product(
                "prod_016",
                "Corsair RM1000x (2021)",
                199.99,
                179.99,
                20,
                4.8,
                "Fuentes de Alimentación",
                "Fuente 1000W 80+ Gold totalmente modular",
                Arrays.asList("https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_016%2FIMG%2FOIP%20(10).jpg?alt=media&token=08dafc10-ad70-449f-be11-15220351a11d",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_016%2FIMG%2FOIP%20(11).jpg?alt=media&token=4d43f43f-f04f-4b6a-bea5-fa56f11744bf",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_016%2FIMG%2FR%20(1).jpg?alt=media&token=42244fa5-8f70-4ac7-aac1-cb09139ff976",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_016%2FIMG%2Fx7a48xEWhoZWkdPuiWxN8S-1200-80.jpg?alt=media&token=be87a444-57e4-416f-b335-d7f778f75ebd"),
                "",
                "",
                new HashMap<String, String>() {{
                    put("Potencia", "1000W");
                    put("Certificación", "80+ Gold");
                    put("Modularidad", "Totalmente modular");
                    put("Ventilador", "135mm Mag-Lev");
                    put("Conectores PCIe", "6x 6+2 pin");
                    put("Garantía", "10 años");
                }}
        );
        mDatabase.child("products").child("prod_016").setValue(product16);

        Product product17 = new Product(
                "prod_017",
                "Seasonic Prime TX-850",
                229.99,
                209.99,
                15,
                4.9,
                "Fuentes de Alimentación",
                "Fuente 850W 80+ Titanium de máxima eficiencia",
                Arrays.asList("https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_017%2FIMG%2FOIP%20(12).jpg?alt=media&token=ae1d22d7-90a3-4ef4-a8be-099a427b7694",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_017%2FIMG%2FOIP%20(13).jpg?alt=media&token=2d8ccd33-5714-489c-b376-c93afde6253c",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_017%2FIMG%2FOIP%20(14).jpg?alt=media&token=ff821ece-2195-4bd4-aebe-34d972f5fcdd",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_017%2FIMG%2FR%20(2).jpg?alt=media&token=afa0a99b-c9b9-4264-b052-4f9404024a88"),
                "",
                "",
                new HashMap<String, String>() {{
                    put("Potencia", "850W");
                    put("Certificación", "80+ Titanium");
                    put("Modularidad", "Totalmente modular");
                    put("Ventilador", "135mm Hydraulic Dynamic");
                    put("Conectores PCIe", "4x 6+2 pin");
                    put("Garantía", "12 años");
                }}
        );
        mDatabase.child("products").child("prod_017").setValue(product17);

        Product product18 = new Product(
                "prod_018",
                "EVGA SuperNOVA 650 G6",
                119.99,
                109.99,
                30,
                4.7,
                "Fuentes de Alimentación",
                "Fuente 650W 80+ Gold compacta y silenciosa",
                Arrays.asList("https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_018%2FIMG%2FOIP%20(15).jpg?alt=media&token=8e8be5fe-fef7-41bd-9092-4d2d8b7ec6c2",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_018%2FIMG%2FOIP%20(16).jpg?alt=media&token=8f15fe1b-cf64-4672-8ee7-61deb0a61c12",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_018%2FIMG%2FOIP%20(18).jpg?alt=media&token=eb875b7c-fa4e-43ce-91f0-5e312670f5c5",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_018%2FIMG%2FEVGA%20Power%20Supply-220-G6-0850-X1-a7.jpg?alt=media&token=57f071db-8ddb-4edc-97fe-e3442ac9655e"),
                "",
                "",
                new HashMap<String, String>() {{
                    put("Potencia", "650W");
                    put("Certificación", "80+ Gold");
                    put("Modularidad", "Totalmente modular");
                    put("Ventilador", "120mm HDB");
                    put("Conectores PCIe", "2x 6+2 pin");
                    put("Garantía", "10 años");
                }}
        );
        mDatabase.child("products").child("prod_018").setValue(product18);

        // Refrigeración
        Product product19 = new Product(
                "prod_019",
                "Noctua NH-D15",
                109.99,
                99.99,
                40,
                4.9,
                "Refrigeración",
                "Disipador de aire de doble torre con dos ventiladores NF-A15",
                Arrays.asList("https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_019%2FIMG%2F5f2529d4-0f92-4a4e-a001-41bfe631bf20.png?alt=media&token=dca80ce0-7ce0-4d20-a5be-84454481c639",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_019%2FIMG%2F6308848_6.webp?alt=media&token=d5a7dc8f-21d7-43f2-ac43-f8bb011abffc",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_019%2FIMG%2F91t48GBv8TL.jpg?alt=media&token=3fc33713-d644-48a7-9dc9-01212ed58acd",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_019%2FIMG%2FOIP%20(19).jpg?alt=media&token=331e3243-d8f5-4393-882b-2ad1202ae798"),
                "",
                "",
                new HashMap<String, String>() {{
                    put("Tipo", "Disipador de aire");
                    put("Altura", "165mm");
                    put("Ventiladores", "2x NF-A15 PWM");
                    put("TDP", "220W");
                    put("Ruido", "24.6 dB(A)");
                    put("Compatibilidad", "Intel/AMD");
                }}
        );
        mDatabase.child("products").child("prod_019").setValue(product19);

        Product product20 = new Product(
                "prod_020",
                "Corsair iCUE H150i Elite Capellix",
                199.99,
                179.99,
                25,
                4.8,
                "Refrigeración",
                "Kit de refrigeración líquida AIO 360mm con RGB",
                Arrays.asList("https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_020%2FIMG%2F6534177cv20d.avif?alt=media&token=54969d59-2f47-4db5-b9c0-f8eb7155b54c",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_020%2FIMG%2FCORSAIR_H150i_ELITE_CAPELLIX_11.jpg?alt=media&token=8b0146c7-8d5a-46a7-8258-cbebfcdb7565",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_020%2FIMG%2FOIP%20(20).jpg?alt=media&token=bfa64490-842b-4a77-821b-bcd386c6e235",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_020%2FIMG%2FR%20(3).jpg?alt=media&token=b515a1b8-7a87-4b2e-9efc-0a10db20f275"),
                "",
                "",
                new HashMap<String, String>() {{
                    put("Tipo", "AIO líquido");
                    put("Radiador", "360mm");
                    put("Ventiladores", "3x ML120 RGB PWM");
                    put("TDP", "300W+");
                    put("Ruido", "10-36 dB(A)");
                    put("Compatibilidad", "Intel/AMD");
                }}
        );
        mDatabase.child("products").child("prod_020").setValue(product20);

        Product product21 = new Product(
                "prod_021",
                "Lian Li Galahad AIO 240",
                149.99,
                139.99,
                30,
                4.7,
                "Refrigeración",
                "Refrigeración líquida con bomba de alto flujo y ventiladores RGB",
                Arrays.asList("https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_021%2FIMG%2FOIP%20(21).jpg?alt=media&token=893283f9-7980-49cf-b96e-56667f100c0c",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_021%2FIMG%2FOIP%20(22).jpg?alt=media&token=ba07c7f8-c63f-4e5f-a012-d0588a18bd21",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_021%2FIMG%2FOIP%20(23).jpg?alt=media&token=36853fb5-482d-43fc-9ba9-226222bff3c4",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_021%2FIMG%2FOIP%20(24).jpg?alt=media&token=03cd9877-c912-434f-b3b3-5438150470e0"),
                "",
                "",
                new HashMap<String, String>() {{
                    put("Tipo", "AIO líquido");
                    put("Radiador", "240mm");
                    put("Ventiladores", "2x 120mm RGB");
                    put("TDP", "300W");
                    put("Ruido", "17.3-30.2 dB(A)");
                    put("Compatibilidad", "Intel/AMD");
                }}
        );
        mDatabase.child("products").child("prod_021").setValue(product21);

        // Periféricos
        Product product22 = new Product(
                "prod_022",
                "Logitech G Pro X Superlight",
                149.99,
                129.99,
                50,
                4.8,
                "Periféricos",
                "Ratón gaming inalámbrico ultraligero (63g) con sensor HERO 25K",
                Arrays.asList("https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_022%2FIMG%2FOIP%20(25).jpg?alt=media&token=3569105a-e371-426c-b92e-58b380fca229",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_022%2FIMG%2FOIP%20(26).jpg?alt=media&token=aece1004-bc16-412d-95d1-eadf62df8b67",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_022%2FIMG%2FOIP%20(27).jpg?alt=media&token=0d22aaa9-3e53-428e-9efd-dd8d59c51b32",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_022%2FIMG%2Fdesign-large.jpg?alt=media&token=81c4062c-3d81-42d1-b762-0a070932c68b"),
                "",
                "",
                new HashMap<String, String>() {{
                    put("Tipo", "Ratón inalámbrico");
                    put("Sensor", "HERO 25K");
                    put("DPI", "100-25,600");
                    put("Peso", "63g");
                    put("Botones", "5 programables");
                    put("Batería", "70 horas");
                }}
        );
        mDatabase.child("products").child("prod_022").setValue(product22);

        Product product23 = new Product(
                "prod_023",
                "Razer BlackWidow V3 Pro",
                229.99,
                199.99,
                25,
                4.7,
                "Periféricos",
                "Teclado mecánico inalámbrico con switches Green clicky",
                Arrays.asList("https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_023%2FIMG%2F6425357_sd.avif?alt=media&token=a27fe0a8-56f9-4e49-b6b4-b931ae3b4c43",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_023%2FIMG%2FMg6EdDNVJjG5dTnww9S6RE-480-80.jpg?alt=media&token=7501f9f3-5e3d-4397-b4b6-d569d3298023",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_023%2FIMG%2FOIP%20(28).jpg?alt=media&token=9e00f1f8-f32a-40c9-98a7-0fff66f43f5a",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_023%2FIMG%2FOIP%20(29).jpg?alt=media&token=9b15a675-dc3f-4986-86fd-8f0ccf636401"),
                "",
                "",
                new HashMap<String, String>() {{
                    put("Tipo", "Teclado mecánico inalámbrico");
                    put("Switches", "Razer Green");
                    put("Iluminación", "RGB Chroma");
                    put("Conectividad", "Bluetooth/2_4GHz/USB");
                    put("Batería", "Hasta 200 horas");
                    put("Layout", "US QWERTY");
                }}
        );
        mDatabase.child("products").child("prod_023").setValue(product23);

        Product product24 = new Product(
                "prod_024",
                "SteelSeries Arctis Nova Pro",
                349.99,
                329.99,
                15,
                4.9,
                "Periféricos",
                "Auriculares gaming con sonido Hi-Res y cancelación activa de ruido",
                Arrays.asList("https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_024%2FIMG%2FOIP%20(30).jpg?alt=media&token=e4ae2a51-ae22-445c-8a1b-037e447ace26",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_024%2FIMG%2FOIP%20(31).jpg?alt=media&token=2c8ce313-584d-4dde-b4f9-60c4d0560beb",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_024%2FIMG%2FOIP%20(32).jpg?alt=media&token=336f373c-d2b9-480c-b0ea-59c16678d764",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_024%2FIMG%2FOIP%20(32).jpg?alt=media&token=336f373c-d2b9-480c-b0ea-59c16678d764"),
                "",
                "",
                new HashMap<String, String>() {{
                    put("Tipo", "Auriculares gaming");
                    put("Conectividad", "Inalámbrico/3.5mm/USB");
                    put("Frecuencia", "10-22,000 Hz");
                    put("Micrófono", "Retráctil con cancelación de ruido");
                    put("Batería", "Hasta 22 horas");
                    put("Compatibilidad", "PC, PS5, Xbox, Switch");
                }}
        );
        mDatabase.child("products").child("prod_024").setValue(product24);
    }

    private void insertCategories() {
        // Categoría 1: Procesadores
        Category category1 = new Category(
                "Procesadores",
                "Unidades centrales de procesamiento para todo tipo de necesidades",
                ""
        );
        category1.setProductCount(15);
        mDatabase.child("categories").child("cat_001").setValue(category1);

        // Categoría 2: Tarjetas Gráficas
        Category category2 = new Category(
                "Tarjetas Gráficas",
                "Unidades de procesamiento gráfico para gaming y creación de contenido",
                ""
        );
        category2.setProductCount(12);
        mDatabase.child("categories").child("cat_002").setValue(category2);

        // Categoría 3: Memoria RAM
        Category category3 = new Category(
                "Memoria RAM",
                "Módulos de memoria de acceso aleatorio para mejorar el rendimiento",
                ""
        );
        category3.setProductCount(20);
        mDatabase.child("categories").child("cat_003").setValue(category3);

        // Categoría 4: Almacenamiento
        Category category4 = new Category(
                "Almacenamiento",
                "SSDs, HDDs y unidades de almacenamiento para todos los usos",
                ""
        );
        category4.setProductCount(25);
        mDatabase.child("categories").child("cat_004").setValue(category4);

        // Categoría 5: Placas Base
        Category category5 = new Category(
                "Placas Base",
                "Motherboards para todas las plataformas y formatos",
                ""
        );
        category5.setProductCount(18);
        mDatabase.child("categories").child("cat_005").setValue(category5);

        // Categoría 6: Fuentes de Alimentación
        Category category6 = new Category(
                "Fuentes de Alimentación",
                "PSUs para alimentar tu sistema de forma eficiente",
                ""
        );
        category6.setProductCount(15);
        mDatabase.child("categories").child("cat_006").setValue(category6);

        // Categoría 7: Refrigeración
        Category category7 = new Category(
                "Refrigeración",
                "Soluciones de enfriamiento para mantener tu sistema a temperaturas óptimas",
                ""
        );
        category7.setProductCount(22);
        mDatabase.child("categories").child("cat_007").setValue(category7);

        // Categoría 8: Periféricos
        Category category8 = new Category(
                "Periféricos",
                "Dispositivos de entrada y salida para completar tu setup",
                ""
        );
        category8.setProductCount(30);
        mDatabase.child("categories").child("cat_008").setValue(category8);
    }

    /**
     * Insert gift cards into the database
     */
    private void insertGiftCards() {

        GiftCard specialGiftCard = new GiftCard(100.00, "admin"); // Set your desired amount and creator
        specialGiftCard.setCardId("gc_special_001"); // Set a unique ID
        specialGiftCard.setCode("PDM-SUFIII"); // Override the generated code with your specific code

// Set dates (optional, the constructor already sets them)
        specialGiftCard.setCreationDate(new Date());
        specialGiftCard.setExpirationDate(new Date(System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000))); // 1 year

// Save to Firebase
        mDatabase.child("giftCards").child(specialGiftCard.getCardId()).setValue(specialGiftCard)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Special gift card created successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error creating special gift card", e));

        // GiftCard 1
        GiftCard gc1 = new GiftCard(GiftCard.generateGiftCardCode(), 50.00, "admin1");
        gc1.setCardId("gc_001");
        mDatabase.child("giftCards").child(gc1.getCardId()).setValue(gc1);

        // GiftCard 2
        GiftCard gc2 = new GiftCard(GiftCard.generateGiftCardCode(), 25.00, "admin1");
        gc2.setCardId("gc_002");
        mDatabase.child("giftCards").child(gc2.getCardId()).setValue(gc2);

        // GiftCard 3
        GiftCard gc3 = new GiftCard(GiftCard.generateGiftCardCode(), 100.00, "admin2");
        gc3.setCardId("gc_003");
        mDatabase.child("giftCards").child(gc3.getCardId()).setValue(gc3);

        // GiftCard 4 (Redimida)
        GiftCard gc4 = new GiftCard(GiftCard.generateGiftCardCode(), 75.00, "admin1");
        gc4.setCardId("gc_004");
        gc4.setStatus("redeemed");
        gc4.setRedeemedBy("user123");
        gc4.setRedeemedDate(new Date());
        mDatabase.child("giftCards").child(gc4.getCardId()).setValue(gc4);

        // GiftCard 5
        GiftCard gc5 = new GiftCard(GiftCard.generateGiftCardCode(), 150.00, "admin3");
        gc5.setCardId("gc_005");
        mDatabase.child("giftCards").child(gc5.getCardId()).setValue(gc5);

        // GiftCard 6 (Expirada)
        GiftCard gc6 = new GiftCard(GiftCard.generateGiftCardCode(), 30.00, "admin2");
        gc6.setCardId("gc_006");
        gc6.setExpirationDate(new Date(System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000))); // Expiró hace 30 días
        mDatabase.child("giftCards").child(gc6.getCardId()).setValue(gc6);

        // GiftCard 7
        GiftCard gc7 = new GiftCard(GiftCard.generateGiftCardCode(), 200.00, "admin1");
        gc7.setCardId("gc_007");
        mDatabase.child("giftCards").child(gc7.getCardId()).setValue(gc7);

        // GiftCard 8
        GiftCard gc8 = new GiftCard(GiftCard.generateGiftCardCode(), 10.00, "admin3");
        gc8.setCardId("gc_008");
        mDatabase.child("giftCards").child(gc8.getCardId()).setValue(gc8);

        // GiftCard 9 (Redimida)
        GiftCard gc9 = new GiftCard(GiftCard.generateGiftCardCode(), 50.00, "admin2");
        gc9.setCardId("gc_009");
        gc9.setStatus("redeemed");
        gc9.setRedeemedBy("user456");
        gc9.setRedeemedDate(new Date(System.currentTimeMillis() - (15L * 24 * 60 * 60 * 1000))); // Redimida hace 15 días
        mDatabase.child("giftCards").child(gc9.getCardId()).setValue(gc9);

        // GiftCard 10
        GiftCard gc10 = new GiftCard(GiftCard.generateGiftCardCode(), 125.00, "admin1");
        gc10.setCardId("gc_010");
        mDatabase.child("giftCards").child(gc10.getCardId()).setValue(gc10);
    }

    /**
     * Insert discount codes into the database
     */
    private void insertDiscountCodes() {

        // Código 1: Descuento del 10%
        DiscountCode code1 = new DiscountCode(
                "LIVIN10",
                10.0,
                "admin1"
        );
        code1.setCodeId("dc_001");
        code1.setMinPurchaseRequired(50.0);
        mDatabase.child("discountCodes").child(code1.getCodeId()).setValue(code1);

        // Código 2: Descuento del 15%
        DiscountCode code2 = new DiscountCode(
                "GAMING15",
                15.0,
                "admin1"
        );
        code2.setCodeId("dc_002");
        mDatabase.child("discountCodes").child(code2.getCodeId()).setValue(code2);

        // Código 3: Descuento del 20% (mínimo $100)
        DiscountCode code3 = new DiscountCode(
                "BUILDPC20",
                20.0,
                "admin2"
        );
        code3.setCodeId("dc_003");
        code3.setMinPurchaseRequired(100.0);
        mDatabase.child("discountCodes").child(code3.getCodeId()).setValue(code3);

        // Código 4: Descuento del 5%
        DiscountCode code4 = new DiscountCode(
                "UPGRADE5",
                5.0,
                "admin1"
        );
        code4.setCodeId("dc_004");
        mDatabase.child("discountCodes").child(code4.getCodeId()).setValue(code4);

        // Código 5: Descuento del 25% (para compras mayores a $200)
        DiscountCode code5 = new DiscountCode(
                "PREMIUM25",
                25.0,
                "admin3"
        );
        code5.setCodeId("dc_005");
        code5.setMinPurchaseRequired(200.0);
        mDatabase.child("discountCodes").child(code5.getCodeId()).setValue(code5);

        // Código 6: Descuento del 10% en componentes de refrigeración
        DiscountCode code6 = new DiscountCode(
                "COOL10",
                10.0,
                "admin2"
        );
        code6.setCodeId("dc_006");
        mDatabase.child("discountCodes").child(code6.getCodeId()).setValue(code6);

        // Código 7: Descuento del 30% (promoción especial)
        DiscountCode code7 = new DiscountCode(
                "FLASH30",
                30.0,
                "admin3"
        );
        code7.setCodeId("dc_007");
        code7.setExpirationDate(new Date(System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000))); // Válido por 7 días
        mDatabase.child("discountCodes").child(code7.getCodeId()).setValue(code7);

        // Código 8: Descuento del 15% en memorias RAM
        DiscountCode code8 = new DiscountCode(
                "RAM15",
                15.0,
                "admin1"
        );
        code8.setCodeId("dc_008");
        mDatabase.child("discountCodes").child(code8.getCodeId()).setValue(code8);

        // Código 9: Descuento del 10% (para primera compra)
        DiscountCode code9 = new DiscountCode(
                "FIRSTBUILD10",
                10.0,
                "admin1"
        );
        code9.setCodeId("dc_009");
        code9.setMinPurchaseRequired(0.0);
        mDatabase.child("discountCodes").child(code9.getCodeId()).setValue(code9);

        // Código 10: Descuento del 50% (cyber monday)
        DiscountCode code10 = new DiscountCode(
                "CYBERMONDAY50",
                50.0,
                "admin1"
        );
        code10.setCodeId("dc_010");
        code10.setMinPurchaseRequired(300.0);
        code10.setExpirationDate(new Date(System.currentTimeMillis() + (2L * 24 * 60 * 60 * 1000))); // Válido por 48 horas
        mDatabase.child("discountCodes").child(code10.getCodeId()).setValue(code10);
    }

    /**
     * Insert departments of El Salvador with their cities and shipping costs into the database
     */
    private void insertDepartamentos() {
        // Departamento 1: San Salvador (ciudad sede: San Salvador)
        Departamento dep1 = new Departamento.Builder("San Salvador")
                .establecerCiudadSede("San Salvador")
                .agregarCiudad("Soyapango")
                .agregarCiudad("Mejicanos")
                .agregarCiudad("Apopa")
                .agregarCiudad("Santa Tecla")
                .conCostoEnvioSede(2.0)
                .conCostoBaseEnvio(4.0)
                .build();
        dep1.setId("dep_001");
        mDatabase.child("departamentos").child(dep1.getId()).setValue(dep1);

        // Departamento 2: La Libertad (ciudad sede: Santa Tecla)
        Departamento dep2 = new Departamento.Builder("La Libertad")
                .establecerCiudadSede("Santa Tecla")
                .agregarCiudad("Antiguo Cuscatlán")
                .agregarCiudad("Zaragoza")
                .agregarCiudad("Colón")
                .conCostoEnvioSede(1.5)
                .conCostoBaseEnvio(7.0)
                .build();
        dep2.setId("dep_002");
        mDatabase.child("departamentos").child(dep2.getId()).setValue(dep2);

        // Departamento 3: San Miguel (ciudad sede: San Miguel)
        Departamento dep3 = new Departamento.Builder("San Miguel")
                .establecerCiudadSede("San Miguel")
                .agregarCiudad("Ciudad Barrios")
                .agregarCiudad("Chinameca")
                .agregarCiudad("Moncagua")
                .conCostoEnvioSede(2.0)
                .conCostoBaseEnvio(5.5)
                .build();
        dep3.setId("dep_003");
        mDatabase.child("departamentos").child(dep3.getId()).setValue(dep3);

        // Departamento 4: Santa Ana (ciudad sede: Santa Ana)
        Departamento dep4 = new Departamento.Builder("Santa Ana")
                .establecerCiudadSede("Santa Ana")
                .agregarCiudad("Chalchuapa")
                .agregarCiudad("Metapán")
                .agregarCiudad("El Congo")
                .conCostoEnvioSede(1.5)
                .conCostoBaseEnvio(6.0)
                .build();
        dep4.setId("dep_004");
        mDatabase.child("departamentos").child(dep4.getId()).setValue(dep4);

        // Departamento 5: Sonsonate (ciudad sede: Sonsonate)
        Departamento dep5 = new Departamento.Builder("Sonsonate")
                .establecerCiudadSede("Sonsonate")
                .agregarCiudad("Izalco")
                .agregarCiudad("Nahuizalco")
                .agregarCiudad("Acajutla")
                .conCostoEnvioSede(1.5)
                .conCostoBaseEnvio(3.0)
                .build();
        dep5.setId("dep_005");
        mDatabase.child("departamentos").child(dep5.getId()).setValue(dep5);

        // Departamento 6: La Paz (ciudad sede: Zacatecoluca)
        Departamento dep6 = new Departamento.Builder("La Paz")
                .establecerCiudadSede("Zacatecoluca")
                .agregarCiudad("Santiago Nonualco")
                .agregarCiudad("San Luis Talpa")
                .agregarCiudad("El Rosario")
                .conCostoEnvioSede(1.5)
                .conCostoBaseEnvio(4.5)
                .build();
        dep6.setId("dep_006");
        mDatabase.child("departamentos").child(dep6.getId()).setValue(dep6);

        // Departamento 7: Usulután (ciudad sede: Usulután)
        Departamento dep7 = new Departamento.Builder("Usulután")
                .establecerCiudadSede("Usulután")
                .agregarCiudad("Santiago de María")
                .agregarCiudad("Jucuapa")
                .agregarCiudad("Berlín")
                .conCostoEnvioSede(1.5)
                .conCostoBaseEnvio(3.35)
                .build();
        dep7.setId("dep_007");
        mDatabase.child("departamentos").child(dep7.getId()).setValue(dep7);
    }}