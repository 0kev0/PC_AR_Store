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
        insertDepartamentos();
        insertProducts();
        insertCategories();
        insertGiftCards();
        insertDiscountCodes();
    }

    /**
     * Insert sample PC products into the database
     */
    private void insertProducts() {
        // Producto 1: Procesador
        Product product1 = new Product(
                "prod_001",
                "Procesador AMD Ryzen 9 5900X",
                499.99,
                399.99,
                25,
                4.8,
                "Procesadores",
                "Procesador de alto rendimiento con 12 núcleos y 24 hilos, perfecto para gaming y tareas exigentes.",
                Arrays.asList(
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_001%2FIMG%2Fryzen_front.jpg?alt=media&token=1c75d566-d32d-4449-b53b-e7f20578a296",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_001%2FIMG%2Fryzen_box.jpg?alt=media&token=5f734764-6b38-4654-b818-f98b7c1511f0"
                ),
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_001%2FMODELO_3D%2Fcpu_model.obj?alt=media&token=c659017a-db13-4450-b5f5-a07e1e9baab8",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_001%2FTEXTURAS%2Fcpu_texture.jpg?alt=media&token=f307028a-0929-4963-8220-b16a8a821d12",
                new HashMap<String, String>() {{
                    put("Núcleos", "12");
                    put("Hilos", "24");
                    put("Frecuencia base", "3.7 GHz");
                    put("Frecuencia turbo", "4.8 GHz");
                    put("Socket", "AM4");
                    put("TDP", "105W");
                    put("Caché L3", "64MB");
                }}
        );
        mDatabase.child("products").child("prod_001").setValue(product1);

        // Producto 2: Tarjeta gráfica
        Product product2 = new Product(
                "prod_002",
                "Tarjeta Gráfica NVIDIA RTX 3080",
                799.99,
                699.99,
                15,
                4.9,
                "Tarjetas Gráficas",
                "Potente GPU con 10GB GDDR6X para gaming 4K y Ray Tracing en tiempo real",
                Arrays.asList(
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_002%2FIMG%2Frtx3080_front.jpg?alt=media&token=aef36664-50ae-482b-92d3-ea267331d631",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_002%2FIMG%2Frtx3080_side.jpg?alt=media&token=aef36664-50ae-482b-92d3-ea267331d631"
                ),
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_002%2FMODELO_3D%2Fgpu_model.obj?alt=media&token=752b2ae5-e2cf-4988-a2ac-cf3d89d80f06",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_002%2FTEXTURAS%2Fgpu_texture.jpg?alt=media&token=5cddbe4b-1316-4421-95f3-f6a3af253e03",
                new HashMap<String, String>() {{
                    put("VRAM", "10GB GDDR6X");
                    put("Núcleos CUDA", "8704");
                    put("Reloj base", "1440 MHz");
                    put("Reloj boost", "1710 MHz");
                    put("Interfaz", "PCIe 4.0");
                    put("Conectores", "3x DisplayPort 1.4a, 1x HDMI 2.1");
                    put("Alimentación", "320W");
                }}
        );
        mDatabase.child("products").child("prod_002").setValue(product2);

        // Producto 3: Memoria RAM
        Product product3 = new Product(
                "prod_003",
                "Corsair Vengeance RGB Pro 32GB",
                189.99,
                169.99,
                50,
                4.7,
                "Memoria RAM",
                "Kit de memoria RAM DDR4 de 32GB (2x16GB) con iluminación RGB y alto rendimiento",
                Arrays.asList(
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_003%2FIMG%2Fram_front.jpg?alt=media&token=aef36664-50ae-482b-92d3-ea267331d631",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_003%2FIMG%2Fram_lit.jpg?alt=media&token=aef36664-50ae-482b-92d3-ea267331d631",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_003%2FIMG%2Fram_pair.jpg?alt=media&token=a0bcea0b-afd0-41bc-88bf-f456cfdf4b65"
                ),
                "",
                "",
                new HashMap<String, String>() {{
                    put("Capacidad", "32GB (2x16GB)");
                    put("Tecnología", "DDR4");
                    put("Velocidad", "3600MHz");
                    put("Latencia", "CL18");
                    put("Perfil", "XMP 2.0");
                    put("Iluminación", "RGB personalizable");
                    put("Compatibilidad", "Intel y AMD");
                }}
        );
        mDatabase.child("products").child("prod_003").setValue(product3);

        // Producto 4: SSD NVMe
        Product product4 = new Product(
                "prod_004",
                "Samsung 970 EVO Plus 1TB NVMe",
                159.99,
                139.99,
                75,
                4.9,
                "Almacenamiento",
                "SSD NVMe ultrarrápido con 1TB de capacidad y velocidades de lectura de hasta 3500MB/s",
                Arrays.asList(
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_004%2FIMG%2Fssd_front.jpg?alt=media&token=aef36664-50ae-482b-92d3-ea267331d631",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_004%2FIMG%2Fssd_box.jpg?alt=media&token=a0bcea0b-afd0-41bc-88bf-f456cfdf4b65"
                ),
                "",
                "",
                new HashMap<String, String>() {{
                    put("Capacidad", "1TB");
                    put("Interfaz", "PCIe 3.0 x4, NVMe 1.3");
                    put("Lectura secuencial", "3500 MB/s");
                    put("Escritura secuencial", "3300 MB/s");
                    put("Factor de forma", "M.2 2280");
                    put("NAND", "V-NAND 3-bit MLC");
                    put("Garantía", "5 años o 600 TBW");
                }}
        );
        mDatabase.child("products").child("prod_004").setValue(product4);

        // Producto 5: Placa base
        Product product5 = new Product(
                "prod_005",
                "ASUS ROG Strix X570-E Gaming",
                329.99,
                299.99,
                20,
                4.8,
                "Placas Base",
                "Placa base premium ATX con chipset X570, WiFi 6 y soporte PCIe 4.0",
                Arrays.asList(
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_005%2FIMG%2Fmotherboard_top.jpg?alt=media&token=aef36664-50ae-482b-92d3-ea267331d631",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_005%2FIMG%2Fmotherboard_io.jpg?alt=media&token=aef36664-50ae-482b-92d3-ea267331d631",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_005%2FIMG%2Fmotherboard_box.jpg?alt=media&token=a0bcea0b-afd0-41bc-88bf-f456cfdf4b65"
                ),
                "",
                "",
                new HashMap<String, String>() {{
                    put("Chipset", "AMD X570");
                    put("Socket", "AM4");
                    put("Formato", "ATX");
                    put("Memoria", "4 slots DDR4, hasta 128GB");
                    put("PCIe", "2x PCIe 4.0 x16, 1x PCIe 4.0 x4");
                    put("Almacenamiento", "2x M.2 PCIe 4.0, 8x SATA 6Gb/s");
                    put("Conectividad", "WiFi 6, Bluetooth 5.0, 2.5Gb Ethernet");
                    put("USB", "USB 3.2 Gen 2 Type-C, múltiples USB 3.2");
                }}
        );
        mDatabase.child("products").child("prod_005").setValue(product5);
    }

    /**
     * Insert PC component categories into the database
     */
    private void insertCategories() {
        // Categoría 1: Procesadores
        Category category1 = new Category(
                "Procesadores",
                "CPUs para todo tipo de necesidades: gaming, workstation, productividad",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/CATEGORIES%2Fcpu.jpg?alt=media"
        );
        category1.setProductCount(25);
        mDatabase.child("categories").child("cat_001").setValue(category1);

        // Categoría 2: Tarjetas Gráficas
        Category category2 = new Category(
                "Tarjetas Gráficas",
                "GPUs para gaming, renderizado, diseño y más",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/CATEGORIES%2Fgpu.jpg?alt=media"
        );
        category2.setProductCount(18);
        mDatabase.child("categories").child("cat_002").setValue(category2);

        // Categoría 3: Memoria RAM
        Category category3 = new Category(
                "Memoria RAM",
                "Módulos de memoria DDR4 y DDR5 de todos los tamaños",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/CATEGORIES%2Fram.jpg?alt=media"
        );
        category3.setProductCount(15);
        mDatabase.child("categories").child("cat_003").setValue(category3);

        // Categoría 4: Almacenamiento
        Category category4 = new Category(
                "Almacenamiento",
                "SSDs NVMe, SATA y discos duros para todas las necesidades",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/CATEGORIES%2Fstorage.jpg?alt=media"
        );
        category4.setProductCount(30);
        mDatabase.child("categories").child("cat_004").setValue(category4);

        // Categoría 5: Placas Base
        Category category5 = new Category(
                "Placas Base",
                "Motherboards para plataformas Intel y AMD",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/CATEGORIES%2Fmotherboard.jpg?alt=media"
        );
        category5.setProductCount(22);
        mDatabase.child("categories").child("cat_005").setValue(category5);

        // Categoría 6: Fuentes de Alimentación
        Category category6 = new Category(
                "Fuentes de Alimentación",
                "PSUs certificadas 80+ Bronze, Gold, Platinum y Titanium",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/CATEGORIES%2Fpsu.jpg?alt=media"
        );
        category6.setProductCount(12);
        mDatabase.child("categories").child("cat_006").setValue(category6);

        // Categoría 7: Refrigeración
        Category category7 = new Category(
                "Refrigeración",
                "Disipadores, ventiladores y soluciones líquidas para mantener tu PC fresca",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/CATEGORIES%2Fcooling.jpg?alt=media"
        );
        category7.setProductCount(20);
        mDatabase.child("categories").child("cat_007").setValue(category7);

        // Categoría 8: Periféricos
        Category category8 = new Category(
                "Periféricos",
                "Teclados, ratones, auriculares y más para tu configuración",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/CATEGORIES%2Fperipherals.jpg?alt=media"
        );
        category8.setProductCount(45);
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