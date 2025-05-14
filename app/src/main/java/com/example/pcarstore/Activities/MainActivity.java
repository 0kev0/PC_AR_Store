package com.example.pcarstore.Activities;

import android.content.Intent;
import android.net.Uri;
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
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private Button catalogo, loginGoogle, test;
    private FirebaseAuth mAuth;
    private CredentialManager credentialManager;
    private DatabaseReference mDatabase;

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

        catalogo = findViewById(R.id.ContinueWithoutAcount);
        catalogo.setOnClickListener(v -> VerCatologo(v));


        test = findViewById(R.id.test);
        test.setOnClickListener(v -> ARtest(v));

        loginGoogle = findViewById(R.id.LoginGoogle);
        credentialManager = CredentialManager.create(this);

        // Configurar listeners
        loginGoogle.setOnClickListener(v -> registerGoogle());
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
    private void insertProducts() {
        // Producto 1: Llantas deportivas
        Product product1 = new Product(
                "Llantas DeportivasX 18''",
                199.99,
                "Ruedas",
                50,
                "Llantas aleación ligeraX",
                Arrays.asList(
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2FITEM1%2FIMG%2FR.jpeg?alt=media&token=ae438215-0ae2-43e9-9351-0b7691288be7",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2FITEM1%2FIMG%2Fth.jpeg?alt=media&token=4f15e63c-579a-412f-a9ba-58f611ea0886"
                ),
                "https://example.com/llanta.glb"
        );
        product1.setSpecs(new HashMap<String, String>() {{
            put("Material", "Aleación");
            put("Color", "Negro");
            put("Diámetro", "18 pulgadas");
        }});
        mDatabase.child("products").child("prod_001").setValue(product1);

        // Producto 2: Filtro de aire
        Product product2 = new Product(
                "Filtro Aire K&N",
                29.99,
                "Motor",
                100,
                "Filtro reutilizable",
                Arrays.asList(
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2Fth.jpeg?alt=media&token=02ca6552-af45-4ecb-a343-03bdf01412a0",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.appspot.com/PRODUCTS/filtro_detalle1.jpg"
                ),
                ""
        );
        product2.setSpecs(new HashMap<String, String>() {{
            put("Tipo", "Reutilizable");
            put("Flujo de aire", "Mayor 50%");
        }});
        mDatabase.child("products").child("prod_002").setValue(product2);

        // Producto 3: Pastillas de freno
        Product product3 = new Product(
                "Pastillas Brembo",
                45.50,
                "Frenos",
                30,
                "Pastillas cerámicas",
                Arrays.asList(
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/PRODUCTS%2F20250315_154351%20(2).jpg?alt=media&token=e3fc6cf0-ce28-4cb8-b8d0-bb77061d5622",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.appspot.com/PRODUCTS/pastillas_detalle1.jpg",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.appspot.com/PRODUCTS/pastillas_detalle2.jpg"
                ),
                ""
        );
        mDatabase.child("products").child("prod_003").setValue(product3);

        // Producto 4: Aceite sintético
        Product product4 = new Product(
                "Aceite Mobil 1 5W-30",
                39.99,
                "Lubricantes",
                200,
                "Aceite para alto rendimiento",
                Arrays.asList(
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.appspot.com/PRODUCTS/aceite_principal.jpg",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.appspot.com/PRODUCTS/aceite_detalle1.jpg"
                ),
                ""
        );
        mDatabase.child("products").child("prod_004").setValue(product4);

        // Producto 5: Batería
        Product product5 = new Product(
                "Batería Optima RedTop",
                129.99,
                "Eléctrico",
                25,
                "Batería sellada",
                Arrays.asList(
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.appspot.com/PRODUCTS/bateria_principal.jpg",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.appspot.com/PRODUCTS/bateria_lateral.jpg",
                        "https://firebasestorage.googleapis.com/v0/b/pcarstore.appspot.com/PRODUCTS/bateria_especificaciones.jpg"
                ),
                ""
        );
        product5.setSpecs(new HashMap<String, String>() {{
            put("Capacidad", "800 CCA");
            put("Tipo", "Sellada AGM");
        }});
        mDatabase.child("products").child("prod_005").setValue(product5);
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

    private void insertOrders() {
        // Orden 1: Completa (con items)
        Order order1 = new Order("user_001", "completed", 429.97);
        order1.setItems(new HashMap<String, OrderItem>() {{
            put("item_001", new OrderItem(2, 199.99)); // 2 llantas
            put("item_002", new OrderItem(1, 29.99));  // 1 filtro
        }});
        mDatabase.child("orders").child("order_001").setValue(order1);

        // Orden 2: En proceso
        Order order2 = new Order("user_002", "processing", 91.00);
        order2.setItems(new HashMap<String, OrderItem>() {{
            put("item_003", new OrderItem(2, 45.50)); // 2 pastillas
        }});
        mDatabase.child("orders").child("order_002").setValue(order2);

        // Orden 3: Cancelada
        Order order3 = new Order("user_003", "cancelled", 129.99);
        order3.setItems(new HashMap<String, OrderItem>() {{
            put("item_005", new OrderItem(1, 129.99)); // 1 batería
        }});
        mDatabase.child("orders").child("order_003").setValue(order3);

        // Orden 4: Pendiente
        Order order4 = new Order("user_004", "pending", 119.97);
        order4.setItems(new HashMap<String, OrderItem>() {{
            put("item_004", new OrderItem(3, 39.99)); // 3 aceites
        }});
        mDatabase.child("orders").child("order_004").setValue(order4);

        // Orden 5: Con descuento
        Order order5 = new Order("user_005", "completed", 349.98);
        order5.setItems(new HashMap<String, OrderItem>() {{
            put("item_001", new OrderItem(1, 199.99)); // 1 llanta
            put("item_003", new OrderItem(3, 45.50));  // 3 pastillas
        }});
        mDatabase.child("orders").child("order_005").setValue(order5);
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
                                Toast.makeText(MainActivity.this,
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
                        startActivity(new Intent(MainActivity.this, InicioActivity.class));
                        finish();
                    } else if (task == null) {
                        Log.w(TAG, "Error de autenticación", task.getException());
                        Toast.makeText(MainActivity.this,
                                "Error en autenticación con Firebase",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Log.w(TAG, "Error de registro", task.getException());
                        Toast.makeText(MainActivity.this,
                                "Error en autenticación con Firebase",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}