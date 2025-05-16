package com.example.pcarstore.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.example.pcarstore.ModelsDB.Category;
import com.example.pcarstore.ModelsDB.Order;
import com.example.pcarstore.ModelsDB.OrderItem;
import com.example.pcarstore.ModelsDB.Product;
import com.example.pcarstore.ModelsDB.User;
import com.example.pcarstore.R;
import com.example.pcarstore.Services.OrderManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    private Button catalogo, loginGoogle, RegisterGoogle, test;
    private FirebaseAuth mAuth;
    private CredentialManager credentialManager;
    private GoogleSignInClient mGoogleSignInClient;
    private DatabaseReference mDatabase;
    private static final int RC_SIGN_IN = 9001;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mDatabase = FirebaseDatabase.getInstance().getReference();
        insertProducts();
        insertUsers();
        insertOrders();
        insertCategories();

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(LoginActivity.this, InicioActivity.class));
            finish();
            return;
        }

        catalogo = findViewById(R.id.ContinueWithoutAcount);
        catalogo.setOnClickListener(v -> VerCatologo(v));


        test = findViewById(R.id.test);
        test.setOnClickListener(v -> ARtest(v));

        RegisterGoogle = findViewById(R.id.RegisterGoogle);
        credentialManager = CredentialManager.create(this);

        loginGoogle = findViewById(R.id.LoginGoogle);

        // Configurar listeners
        RegisterGoogle.setOnClickListener(v -> registerGoogle());
    }

    private void insertProducts() {
        // Producto 1: Llantas deportivas
        Product product1 = new Product(
                "prod_001",
                "Llantas DeportivasX 18''",
                299.99,
                180.00,
                50,
                4.5,
                "Ruedas",
                "Llantas aleación ligeraX con diseño aerodinámico y mayor durabilidad",
                Arrays.asList(
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_001%2FIMG%2FR.jpeg?alt=media&token=1c75d566-d32d-4449-b53b-e7f20578a296",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_001%2FIMG%2Fvino.webp?alt=media&token=5f734764-6b38-4654-b818-f98b7c1511f0"
                ),
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_001%2FMODELO_3D%2Flemon_4k.obj?alt=media&token=c659017a-db13-4450-b5f5-a07e1e9baab8",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_001%2FTEXTURAS%2Flemon_diff_4k.jpg?alt=media&token=f307028a-0929-4963-8220-b16a8a821d12",
                new HashMap<String, String>() {{
                    put("Material", "Aleación de aluminio");
                    put("Color", "Negro mate");
                    put("Diámetro", "18 pulgadas");
                    put("Ancho", "8.5 pulgadas");
                    put("PCD", "5x114.3");
                    put("Offset", "+35mm");
                    put("Peso", "9.2 kg");
                }}
        );
        mDatabase.child("products").child("prod_001").setValue(product1);

        // Producto 2: Filtro de aire
        Product product2 = new Product(
                "prod_002",
                "Filtro Aire K&N Performance",
                49.99,
                32.50,
                100,
                4.8,
                "Motor",
                "Filtro de aire reutilizable con flujo de aire mejorado y garantía de 1 millón de millas",
                Arrays.asList(
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_002%2FIMG%2FLOGO4.png?alt=media&token=aef36664-50ae-482b-92d3-ea267331d631",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_002%2FIMG%2FLOGO4.png?alt=media&token=aef36664-50ae-482b-92d3-ea267331d631"
                ),
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_002%2FMODELO_3D%2F10201_Potato_v1-L3.obj?alt=media&token=752b2ae5-e2cf-4988-a2ac-cf3d89d80f06",
                "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_002%2FTEXTURAS%2FPotato.jpg?alt=media&token=5cddbe4b-1316-4421-95f3-f6a3af253e03",
                new HashMap<String, String>() {{
                    put("Tipo", "Reutilizable");
                    put("Flujo de aire", "Mayor 50% que filtros estándar");
                    put("Material", "Algodón impregnado en aceite");
                    put("Limpieza", "Cada 80,000 km");
                    put("Garantía", "1,000,000 millas");
                    put("Compatibilidad", "Universal");
                }}
        );
        mDatabase.child("products").child("prod_002").setValue(product2);

        // Producto 3: Pastillas de freno
        Product product3 = new Product(
                "prod_003",
                "Pastillas Brembo Cerámicas",
                89.50,
                60.00,
                30,
                4.7,
                "Frenos",
                "Pastillas de freno cerámicas de alto rendimiento con menor desgaste de discos",
                Arrays.asList(
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_002%2FIMG%2FLOGO4.png?alt=media&token=aef36664-50ae-482b-92d3-ea267331d631",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_002%2FIMG%2FLOGO4.png?alt=media&token=aef36664-50ae-482b-92d3-ea267331d631",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_002%2FIMG%2FLOGO5.png?alt=media&token=a0bcea0b-afd0-41bc-88bf-f456cfdf4b65"
                ),
                "",
                "",
                new HashMap<String, String>() {{
                    put("Material", "Cerámica compuesta");
                    put("Temperatura operación", "0-800°C");
                    put("Dust Level", "Bajo");
                    put("Ruido", "Mínimo");
                    put("Vida útil", "60,000-80,000 km");
                    put("Compatibilidad", "Ver lista de aplicaciones");
                }}
        );
        mDatabase.child("products").child("prod_003").setValue(product3);

        // Producto 4: Aceite sintético
        Product product4 = new Product(
                "prod_004",
                "Aceite Mobil 1 5W-30 Full Synthetic",
                59.99,
                38.75,
                200,
                4.9,
                "Lubricantes",
                "Aceite sintético de última generación para protección extrema del motor",
                Arrays.asList(
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_002%2FIMG%2FLOGO4.png?alt=media&token=aef36664-50ae-482b-92d3-ea267331d631",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fprod_002%2FIMG%2FLOGO5.png?alt=media&token=a0bcea0b-afd0-41bc-88bf-f456cfdf4b65"
                ),
                "",
                "",
                new HashMap<String, String>() {{
                    put("Tipo", "Sintético completo");
                    put("Viscosidad", "5W-30");
                    put("API", "SN Plus");
                    put("Capacidad", "1 Litro");
                    put("Intervalo cambio", "15,000-20,000 km");
                    put("Protección turbo", "Sí");
                    put("Aditivos", "Anti-desgaste avanzado");
                }}
        );
        mDatabase.child("products").child("prod_004").setValue(product4);

        // Producto 5: Batería
        Product product5 = new Product(
                "prod_005",
                "Batería Optima RedTop 34R",
                189.99,
                125.00,
                25,
                4.6,
                "Eléctrico",
                "Batería sellada AGM de alto rendimiento para arranques en condiciones extremas",
                Arrays.asList(
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.appspot.com/PRODUCTS/bateria_principal.jpg",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.appspot.com/PRODUCTS/bateria_lateral.jpg",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.appspot.com/PRODUCTS/bateria_especificaciones.jpg"
                ),
                "",
                "",
                new HashMap<String, String>() {{
                    put("Tecnología", "AGM SpiralCell");
                    put("Voltaje", "12V");
                    put("CCA", "800 Amperios");
                    put("RC", "100 minutos");
                    put("Ciclos carga", "3x más que baterías convencionales");
                    put("Posición instalación", "Cualquier ángulo");
                    put("Vibración", "Resistente 15x más");
                }}
        );
        mDatabase.child("products").child("prod_005").setValue(product5);
    }

    private void insertOrders() {
        // Productos de ejemplo
        OrderItem llantas = new OrderItem("prod001", "Llantas Premium", 199.99, 1);
        OrderItem filtro = new OrderItem("prod002", "Filtro de Aire", 29.99, 1);
        OrderItem pastillas = new OrderItem("prod003", "Pastillas de Freno", 45.50, 2);
        OrderItem aceite = new OrderItem("prod004", "Aceite Sintético", 39.99, 3);
        OrderItem bateria = new OrderItem("prod005", "Batería 12V", 129.99, 1);

        // Crear órdenes de ejemplo
        Order[] sampleOrders = {
                createOrder("user_001", "completed", 429.97, new OrderItem[]{llantas, llantas, filtro}),
                createOrder("user_002", "processing", 91.00, new OrderItem[]{pastillas}),
                createOrder("user_003", "cancelled", 129.99, new OrderItem[]{bateria}),
                createOrder("user_004", "pending", 119.97, new OrderItem[]{aceite}),
                createOrder("user_005", "completed", 349.98, new OrderItem[]{llantas, pastillas})
        };

        // Insertar órdenes en Firebase
        OrderManager orderManager = OrderManager.getInstance();
        for (int i = 0; i < sampleOrders.length; i++) {
            sampleOrders[i].setOrderId("order_" + (i + 1));
            orderManager.placeOrder(sampleOrders[i]);
        }
    }

    private Order createOrder(String userId, String status, double total, OrderItem[] items) {
        Order order = new Order(userId, status, total);
        for (int i = 0; i < items.length; i++) {
            order.getItems().put("item_" + (i + 1), items[i]);
        }
        return order;
    }

    private void insertCategories() {
        // Categoría 1: Ruedas
        Category category1 = new Category(
                "Ruedas",
                "Llantas, neumáticos y accesorios para ruedas",
                "https://example.com/ruedas.jpg"
        );
        category1.setProductCount(15);
        mDatabase.child("categories").child("cat_001").setValue(category1);

        // Categoría 2: Motor
        Category category2 = new Category(
                "Motor",
                "Componentes y accesorios para el motor",
                "https://example.com/motor.jpg"
        );
        category2.setProductCount(32);
        mDatabase.child("categories").child("cat_002").setValue(category2);

        // Categoría 3: Frenos
        Category category3 = new Category(
                "Frenos",
                "Sistemas de frenado y componentes",
                "https://example.com/frenos.jpg"
        );
        category3.setProductCount(18);
        mDatabase.child("categories").child("cat_003").setValue(category3);

        // Categoría 4: Lubricantes
        Category category4 = new Category(
                "Lubricantes",
                "Aceites y fluidos para el vehículo",
                "https://example.com/lubricantes.jpg"
        );
        category4.setProductCount(25);
        mDatabase.child("categories").child("cat_004").setValue(category4);

        // Categoría 5: Eléctrico
        Category category5 = new Category(
                "Eléctrico",
                "Componentes del sistema eléctrico",
                "https://example.com/electrico.jpg"
        );
        category5.setProductCount(12);
        mDatabase.child("categories").child("cat_005").setValue(category5);
    }

    private void insertUsers() {
        // Usuario 1: Administrador
        User user1 = new User("admin@example.com", "Admin Principal", "admin");
        user1.setCart(new HashMap<String, Integer>() {{
            put("prod_001", 2); // 2 llantas en carrito
        }});
        mDatabase.child("users").child("user_001").setValue(user1);

        // Usuario 2: Cliente normal
        User user2 = new User("cliente1@example.com", "Juan Pérez", "customer");
        user2.setCart(new HashMap<String, Integer>() {{
            put("prod_003", 1); // 1 pastilla de freno
        }});
        mDatabase.child("users").child("user_002").setValue(user2);

        // Usuario 3: Cliente premium
        User user3 = new User("premium@example.com", "María García", "premium");
        user3.setCart(new HashMap<String, Integer>() {{
            put("prod_002", 3); // 3 filtros de aire
        }});
        mDatabase.child("users").child("user_003").setValue(user3);

        // Usuario 4: Técnico
        User user4 = new User("tecnico@taller.com", "Carlos López", "technician");
        mDatabase.child("users").child("user_004").setValue(user4);

        // Usuario 5: Cliente nuevo
        User user5 = new User("nuevo@cliente.com", "Ana Martínez", "customer");
        mDatabase.child("users").child("user_005").setValue(user5);
    }

    public void VerCatologo(View view) {
        startActivity(new Intent(this, InicioActivity.class));
    }

    public void ARtest(View view) {
        startActivity(new Intent(this, AR_test.class));
    }

    private void registerGoogle() {
        GetGoogleIdOption getGoogleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(getGoogleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                this,
                request,
                new CancellationSignal(),
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse getCredentialResponse) {
                        handleSignIn(getCredentialResponse.getCredential());
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Log.e(TAG, "Error en login con Google", e);
                        runOnUiThread(() ->
                                Toast.makeText(LoginActivity.this,
                                        "Error en autenticación: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show());
                    }
                }
        );
    }

    private void handleSignIn(Credential credential) {
        if (credential instanceof CustomCredential) {
            if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(
                    ((CustomCredential) credential).getType())) {
                GoogleIdTokenCredential googleIdTokenCredential =
                        GoogleIdTokenCredential.createFrom(((CustomCredential) credential).getData());
                firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());
            }
        } else {
            Log.e(TAG, "Credencial no esperada: " + credential.getClass().getName());
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Usuario registrado con éxito");
                        startActivity(new Intent(LoginActivity.this, InicioActivity.class));
                        finish();
                    } else if (task == null) {
                        Log.w(TAG, "Error de autenticación", task.getException());
                        Toast.makeText(LoginActivity.this,
                                "Error en autenticación con Firebase",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Log.w(TAG, "Error de registro", task.getException());
                        Toast.makeText(LoginActivity.this,
                                "Error en autenticación con Firebase",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Manejo del resultado si usas startActivityForResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    public void goToRegister(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
}