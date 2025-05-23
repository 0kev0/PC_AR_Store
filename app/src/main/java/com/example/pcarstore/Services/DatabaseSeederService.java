package com.example.pcarstore.Services;

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
 * Service class for initializing database data
 * Handles all insert operations for products, categories, gift cards, and discount codes
 *
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
        //insertGiftCards();
        //insertDiscountCodes();
    }

    /**
     * Insert sample PC products into the database
     */
    private void insertProducts() {
        // Procesadores
        Product product1 = new Product(
                "prod_001",
                "pequeno Intel Core i9-13900K",
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
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_001%2FTEXTURAS%2Flemon_diff_4k.jpg?alt=media&token=f307028a-0929-4963-8220-b16a8a821d12",
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
        mDatabase.child("products").child("prod_001").setValue(product1);

        Product product2 = new Product(
                "prod_002",
                "bien grafica",
                449.99,
                399.99,
                25,
                4.8,
                "Procesadores",
                "Procesador con tecnología 3D V-Cache para gaming de alto rendimiento",
                Arrays.asList("", ""),
"https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_003%2FMODELO_3D%2FUntitled.obj?alt=media&token=3e39b974-9e49-4a65-8f91-b8d6070b271d",
"https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_003%2FTEXTURA%2FtexturaGenerica.jpg?alt=media&token=7a78ad20-eff2-4d48-a942-47ecc0ec0c83",
                new HashMap<String, String>() {{
                    put("Núcleos", "8");
                    put("Hilos", "16");
                    put("Frecuencia base", "4.2 GHz");
                    put("Frecuencia turbo", "5.0 GHz");
                    put("Socket", "AM5");
                    put("TDP", "120W");
                    put("Caché L3", "96MB");
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
                "",
                "",
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
                "teclado *grande",
                1599.99,
                1499.99,
                10,
                4.9,
                "Tarjetas Gráficas",
                "La GPU más potente para gaming 8K y creación de contenido",
                Arrays.asList("", ""),
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_004%2FMODELO_3D%2FUntiteclado.obj?alt=media&token=f5d051ec-a007-42bf-8681-921794b215f6+",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_004%2FTEXTURAS%2FtexturaGenerica.jpg?alt=media&token=8648eba1-a65e-4833-b529-75f6c40ecc83",
                new HashMap<String, String>() {{
                    put("VRAM", "24GB GDDR6X");
                    put("Núcleos CUDA", "16384");
                    put("Reloj boost", "2520 MHz");
                    put("Interfaz", "PCIe 4.0");
                    put("TDP", "450W");
                    put("Conectores", "3x DisplayPort 1.4a, 1x HDMI 2.1a");
                }}
        );
        mDatabase.child("products").child("prod_004").setValue(product4);

        Product product5 = new Product(
                "prod_005",
                "AMD RX 7900 XTX * medianamente bien",
                999.99,
                899.99,
                15,
                4.8,
                "Tarjetas Gráficas",
                "GPU AMD de alto rendimiento con arquitectura RDNA 3",
                Arrays.asList("", ""),
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_005%2FMODELO_3D%2FUntitled.obj?alt=media&token=76b62072-bb29-4c7c-aa13-efd7f3587191",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_005%2FTEXTURAS%2FMouse_baseColor.png?alt=media&token=d99833da-38f3-40af-aa09-7482a7547ead",
                new HashMap<String, String>() {{
                    put("VRAM", "24GB GDDR6");
                    put("Ancho de banda", "960 GB/s");
                    put("Reloj boost", "2500 MHz");
                    put("Interfaz", "PCIe 4.0");
                    put("TDP", "355W");
                    put("Conectores", "2x DisplayPort 2.1, 1x HDMI 2.1a");
                }}
        );
        mDatabase.child("products").child("prod_005").setValue(product5);

        Product product6 = new Product(
                "prod_006",
                "auriculares, *pequeno",
                799.99,
                749.99,
                20,
                4.7,
                "Tarjetas Gráficas",
                "GPU de gama alta con DLSS 3 y Ray Tracing",
                Arrays.asList("", ""),
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_006%2FMODELO_3D%2FheadphonesSF.obj?alt=media&token=81dad9c7-c9e3-4be0-a6b1-ebe0768efd9b",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_006%2FTEXTURAS%2FHeadphones_Base_Color.png?alt=media&token=f8c6aa33-2d88-4bd9-8fd5-8a68b6669887",
                new HashMap<String, String>() {{
                    put("VRAM", "12GB GDDR6X");
                    put("Núcleos CUDA", "7680");
                    put("Reloj boost", "2610 MHz");
                    put("Interfaz", "PCIe 4.0");
                    put("TDP", "285W");
                    put("Conectores", "3x DisplayPort 1.4a, 1x HDMI 2.1");
                }}
        );
        mDatabase.child("products").child("prod_006").setValue(product6);

        // Memoria RAM
        Product product7 = new Product(
                "prod_007",
                "ram *bien G.Skill Trident Z5 RGB 32GB DDR5-6000",
                199.99,
                179.99,
                35,
                4.8,
                "Memoria RAM",
                "Kit de memoria DDR5 de alto rendimiento con iluminación RGB",
                Arrays.asList("", ""),
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_007%2FMODELO_3D%2FUntitled.obj?alt=media&token=c3f489ea-49b6-442d-a3c0-05e0330cb307",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_007%2FTEXTURAS%2FtexturaGenerica.jpg?alt=media&token=afc6e879-6985-49fc-a3ae-5b378772ca4a",
                new HashMap<String, String>() {{
                    put("Capacidad", "32GB (2x16GB)");
                    put("Tecnología", "DDR5");
                    put("Velocidad", "6000MHz");
                    put("Latencia", "CL36");
                    put("Voltaje", "1.35V");
                    put("Iluminación", "RGB");
                }}
        );
        mDatabase.child("products").child("prod_007").setValue(product7);

        Product product8 = new Product(
                "prod_008",
                "USB *grande Corsair Dominator Platinum RGB 64GB DDR5-5600",
                349.99,
                329.99,
                25,
                4.9,
                "Memoria RAM",
                "Memoria DDR5 premium con iluminación RGB CAPELLIX",
                Arrays.asList("", ""),
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_008%2FMODELO_3D%2FUntitled.obj?alt=media&token=183ab3a4-7b64-4812-9d65-d69a7df5fa63",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_008%2FTEXTURAS%2FtexturaGenerica.jpg?alt=media&token=5fa7e17d-0c99-4ef6-8ca9-1646cfaa5fba ",
                new HashMap<String, String>() {{
                    put("Capacidad", "64GB (2x32GB)");
                    put("Tecnología", "DDR5");
                    put("Velocidad", "5600MHz");
                    put("Latencia", "CL36");
                    put("Voltaje", "1.25V");
                    put("Iluminación", "RGB CAPELLIX");
                }}
        );
        mDatabase.child("products").child("prod_008").setValue(product8);

        Product product9 = new Product(
                "prod_009",
                "*bien Laptop",
                79.99,
                69.99,
                50,
                4.6,
                "Memoria RAM",
                "Memoria DDR4 de bajo perfil y alto rendimiento",
                Arrays.asList("", ""),
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_009%2FMODELOS_3D%2FUntitled.obj?alt=media&token=180a76d7-3015-4186-821d-2a361b8fba18",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_009%2FTEXTURAS%2FLaptop1_baseColor.png?alt=media&token=de697db7-ca04-487a-bc02-7471812a27f9",
                new HashMap<String, String>() {{
                    put("Capacidad", "16GB (2x8GB)");
                    put("Tecnología", "DDR4");
                    put("Velocidad", "3200MHz");
                    put("Latencia", "CL16");
                    put("Voltaje", "1.35V");
                    put("Perfil", "Bajo perfil");
                }}
        );
        mDatabase.child("products").child("prod_009").setValue(product9);

        // Almacenamiento
        Product product10 = new Product(
                "prod_010",
                "Samsung 980 Pro 2TB PCIe 4.0 NVMe",
                249.99,
                229.99,
                40,
                4.9,
                "Almacenamiento",
                "SSD NVMe de alto rendimiento con interfaz PCIe 4.0",
                Arrays.asList("", ""),
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_010%2FMOELDO_3D%2FUntitled.obj?alt=media&token=851e5ce9-803b-4a6a-bfb4-5763f7e30256",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_010%2FTEXTURAS%2FNone_baseColor.png?alt=media&token=13ced338-472e-4cfb-836a-0cbdb33c3069",
                new HashMap<String, String>() {{
                    put("Capacidad", "2TB");
                    put("Interfaz", "PCIe 4.0 x4");
                    put("Lectura", "7000 MB/s");
                    put("Escritura", "5100 MB/s");
                    put("Factor de forma", "M2_2280");
                    put("Garantía", "5 años o 1200 TBW");
                }}
        );
        mDatabase.child("products").child("prod_010").setValue(product10);

        Product product11 = new Product(
                "prod_011",
                "uiia i u i i i i ai",
                149.99,
                129.99,
                45,
                4.8,
                "Almacenamiento",
                "SSD NVMe para gaming con heatsink incluido",
                Arrays.asList("", ""),
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_011%2FMODELOS_3D%2FUntitled.obj?alt=media&token=a77545d1-8c31-408b-b81d-53f97a534907",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_011%2FTEXTURAS%2FMaterial.001_baseColor.png?alt=media&token=467f39c8-2f1c-4333-8026-5654215cca90",
                new HashMap<String, String>() {{
                    put("Capacidad", "1TB");
                    put("Interfaz", "PCIe 4.0 x4");
                    put("Lectura", "7300 MB/s");
                    put("Escritura", "6300 MB/s");
                    put("Factor de forma", "M2_2280");
                    put("Garantía", "5 años o 600 TBW");
                }}
        );
        mDatabase.child("products").child("prod_011").setValue(product11);

        Product product12 = new Product(
                "prod_012",
                "SSD",
                119.99,
                109.99,
                30,
                4.7,
                "Almacenamiento",
                "Disco duro para NAS con tecnología CMR y 7200 RPM",
                Arrays.asList("", ""),
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_012%2FMODELOS_3D%2FSSD_M2.obj?alt=media&token=e5df14e3-35b7-4f76-8aa8-114a323e57f0",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_012%2FTEXTURAS%2Fne.png?alt=media&token=de70d137-caa2-48c9-893d-24f8507032a4",
                new HashMap<String, String>() {{
                    put("Capacidad", "4TB");
                    put("Velocidad", "7200 RPM");
                    put("Interfaz", "SATA 6Gb/s");
                    put("Caché", "64MB");
                    put("Tecnología", "CMR");
                    put("Garantía", "3 años");
                }}
        );
        mDatabase.child("products").child("prod_012").setValue(product12);

        // Placas Base
        Product product13 = new Product(
                "prod_013",
                "ASUS ROG Maximus Z790 Hero",
                599.99,
                549.99,
                15,
                4.9,
                "Placas Base",
                "Placa base premium para Intel de 13ª generación con WiFi 6E",
                Arrays.asList("", ""),
                "",
                "",
                new HashMap<String, String>() {{
                    put("Chipset", "Intel Z790");
                    put("Socket", "LGA 1700");
                    put("Formato", "ATX");
                    put("Memoria", "4x DDR5 hasta 128GB");
                    put("PCIe", "1x PCIe 5.0 x16, 1x PCIe 4.0 x16");
                    put("M_2", "5x slots M_2");
                    put("Red", "WiFi 6E, 2.5Gb Ethernet");
                }}
        );
        mDatabase.child("products").child("prod_013").setValue(product13);

        Product product14 = new Product(
                "prod_014",
                "MSI MAG B650 Tomahawk",
                219.99,
                199.99,
                25,
                4.7,
                "Placas Base",
                "Placa base AMD AM5 con sólida VRM y conectividad",
                Arrays.asList("", ""),
                "",
                "",
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
                "Gigabyte X670E Aorus Master",
                499.99,
                469.99,
                10,
                4.8,
                "Placas Base",
                "Placa base flagship para Ryzen 7000 con PCIe 5.0 completo",
                Arrays.asList("", ""),
                "",
                "",
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
                Arrays.asList("", ""),
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
                Arrays.asList("", ""),
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
                Arrays.asList("", ""),
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
                Arrays.asList("", ""),
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
                Arrays.asList("", ""),
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
                Arrays.asList("", ""),
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
                Arrays.asList("", ""),
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
                Arrays.asList("", ""),
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
                Arrays.asList("", ""),
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
                "NEWSETUP10",
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
     * Insert departments with their cities and shipping costs into the database
     */
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