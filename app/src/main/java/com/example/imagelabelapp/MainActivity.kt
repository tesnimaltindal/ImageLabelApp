package com.example.imagelabelapp
//ML KIT : https://developers.google.com/ml-kit/vision/image-labeling/label-map?hl=tr


import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

class MainActivity : AppCompatActivity() {

    private lateinit var objectImage: ImageView //Gösterilecek resim burada tutulur.
    private lateinit var labelText: TextView  //Etiket burada tutulur.
    private lateinit var captureImgBtn: Button //Resim çekme butonu burada tutulur.
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent> //Kamera intent burada tutulur, kamera başlatmak için kullanılır
    private lateinit var imageLabeler: ImageLabeler //ML kit burada başlatılır.

    override fun onCreate(savedInstanceState: Bundle?) //onCreate burada başlatılır.
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        //findViewById burada başlatılır.      //Ekrandaki ImageView, TextView ve Button bileşenlerini
        // Kotlin tarafında tanımlanan değişkenlere bağlıyoruz.
        objectImage = findViewById(R.id.objectImage) //Gösterilecek resim burada tutulur.
        labelText = findViewById(R.id.labelText) //Etiket burada tutulur.
        captureImgBtn = findViewById(R.id.captureImgBtn) //Resim çekme butonu burada tutulur.

        checkCameraPermission() //Kamera izni kontrol edilir.

        imageLabeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
        //ML kit burada başlatılır. //Google’ın ML Kit kütüphanesinden ImageLabeler nesnesi oluşturulur.
        // Bu nesne bitmap’ten etiketleme yapar.
       //Burada cameraLauncher, kamera açıldığında dönen sonucu yakalamak için kullanılır.
        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val extras = result.data?.extras
                    val imageBitmap = extras?.getParcelable<Bitmap>("data", Bitmap::class.java)
                    if (imageBitmap != null) {
                        objectImage.setImageBitmap(imageBitmap)
                        // ML işlemi burada yapılabilir (ileride)
                        LabelImage(imageBitmap)
                    } else {
                        labelText.text = "Unable to Capture Image"
                    }
                }
            }

        captureImgBtn.setOnClickListener {
          //  val clickPicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
          //  if (clickPicture.resolveActivity(packageManager) != null) {
          //      cameraLauncher.launch(clickPicture)
            //}
            // resmi buradan değiştirebilirsin
            captureImgBtn.setOnClickListener {
                val drawable = ContextCompat.getDrawable(this, R.drawable.car)
                val bitmap = (drawable as android.graphics.drawable.BitmapDrawable).bitmap
                objectImage.setImageBitmap(bitmap)
                LabelImage(bitmap)
            }


        }
        //Uygulaman çentikli cihazlarda bozulmaz.
        //İçerikler, sistem çubuklarının altına girmeden düzgün görünür.
        //Kullanıcı deneyimi artar, özellikle tam ekran modlarda.
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    //Burada bitmap (resim) InputImage'e dönüştürülür:
    private fun LabelImage(bitmap: Bitmap)
    {
        // imageLabeler.process(inputImage) ile etiketleme işlemi başlatılır.
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        imageLabeler.process(inputImage).addOnSuccessListener{ label->
            displayLabel(label)
        }.addOnFailureListener{ e->
            labelText.text = "Error : ${e.message}"

        }

    }
    //Burada etiketlerden en güvenilir olanı (ilk sıradaki) alınır ve labelText içine yazılır , modelin en yüksek skorlusu
    private fun displayLabel(labels: List<ImageLabel>) {
        if (labels.isNotEmpty()) {
            val mostConfidentLabel = labels[0]
            labelText.text = "${mostConfidentLabel.text}"
        } else {
            labelText.text = "No labels found"
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                0
            )
        }
    }
}
/*
ALGORİTMA ÖZETİ
Kullanım Akışı:
Uygulama başlatılır → onCreate() çalışır.
UI bileşenleri hazırlanır, kamera izni kontrol edilir.
captureImgBtn'e tıklanır:
(Geçici olarak) horse resmi gösterilir.
LabelImage() fonksiyonu çağrılır.
LabelImage() içinde:
Resim InputImage formatına dönüştürülür.
MLKit ile etiketleme yapılır.
Etiket varsa labelText'te gösterilir
*/

/*ML Kit, Google tarafından geliştirilen ve
 mobil uygulamalarda yapay zeka (AI) ve makine öğrenimi (ML)
 özelliklerini kolayca kullanmanı sağlayan bir mobil makine öğrenimi kütüphanesidir.
 Android ve iOS için kullanılabilir.*/

//Bitmap, her pikselin RGBA (Red, Green, Blue, Alpha) renk bilgilerini tutar.
//Hafızada çok yer kaplayabilir çünkü her piksel için renk değeri saklanır.