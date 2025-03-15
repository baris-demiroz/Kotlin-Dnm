package com.example.yemekkitabi.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.room.Room
import com.example.yemekkitabi.databinding.FragmentTarifBinding
import com.example.yemekkitabi.model.Tarif
import com.example.yemekkitabi.roomdb.TarifDao
import com.example.yemekkitabi.roomdb.TarifDatabase
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers





class TarifFragment : Fragment() {
    private var _binding: FragmentTarifBinding? = null
    private val binding get() = _binding!!
    private lateinit var permissionLauncher : ActivityResultLauncher<String> //izin istemek icin
    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent> //galeriye gitmek icin
    private var secilenGorsel : Uri? = null
    private var secilenBitmap : Bitmap? = null
    private val mDisposable = CompositeDisposable()
    private var secilenTarif : Tarif? = null

    private lateinit var db : TarifDatabase
    private lateinit var  tarifDao : TarifDao


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()

        db = Room.databaseBuilder(requireContext(),TarifDatabase::class.java,"Tarifler").build()
        tarifDao = db.tarifDao()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTarifBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.gorsel.setOnClickListener{gorselSec(it)}
        binding.btnSil.setOnClickListener{sil(it)}
        binding.btnKaydet.setOnClickListener{kaydet(it)}

        arguments?.let {
            var bilgi =TarifFragmentArgs.fromBundle(it).bilgi
            var id = TarifFragmentArgs.fromBundle(it).id

            if (bilgi == "") {
                secilenTarif = null
                binding.btnSil.isEnabled = false
                binding.btnKaydet.isEnabled = true
                binding.isimText.setText("")
                binding.malzemeText.setText("")
            }
            else{
                binding.btnKaydet.isEnabled = false
                binding.btnSil.isEnabled = true
                 val id = TarifFragmentArgs.fromBundle(it).id

                mDisposable.add(
                    tarifDao.findById(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleResponseForSelect)
                )

            }

        }



    }

    private fun handleResponseForSelect(tarif: Tarif){
        val bitmap = BitmapFactory.decodeByteArray(tarif.gorsel,0,tarif.gorsel.size)
        binding.gorsel.setImageBitmap(bitmap)
        binding.isimText.setText(tarif.isim)
        binding.malzemeText.setText(tarif.tarif)
        secilenTarif = tarif

    }

    fun gorselSec(view: View){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                //izin verilmemişse buraya giriyor prog
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_MEDIA_IMAGES)){
                    //snacbar seçmemiz lazm kullanıcdan neden izin isteyeceğimizi söylememiz lazım
                    Snackbar.make(view,"Galeriye ulaşmamız lazım!!!",Snackbar.LENGTH_INDEFINITE).setAction("İzin Ver",{
                        //izin isteyeceğiz
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)

                    }).show()
                }
                else{
                    //izin isteyeceğiz
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }
            else{
                //izin verilmiş zaten else ise
                val intentToGalery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGalery)
            }
        }
        else{
            if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                //izin verilmemişse buraya giriyor prog
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_EXTERNAL_STORAGE)){
                    //snacbar seçmemiz lazm kullanıcdan neden izin isteyeceğimizi söylememiz lazım
                    Snackbar.make(view,"Galeriye ulaşmamız lazım!!!",Snackbar.LENGTH_INDEFINITE).setAction("İzin Ver",{
                        //izin isteyeceğiz
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

                    }).show()
                }
                else{
                    //izin isteyeceğiz
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
            else{
                //izin verilmiş zaten else ise
                val intentToGalery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGalery)
            }
        }

    }
    fun sil(view: View){
        if (secilenTarif != null){
            mDisposable.add(
                tarifDao.delete(tarif =  secilenTarif!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseForInsert) //GeriyeGideceğim için handleResponseForInsertü kullandım
            )
        }

    }
    fun kaydet(view: View){
        val isim = binding.isimText.text.toString()
        val malzeme = binding.malzemeText.text.toString()

        if (secilenBitmap != null){
            val kucukBitmap = kucukBitmapOlustur(secilenBitmap!!,300)
            val outputStream = ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteDizisi = outputStream.toByteArray()

            val tarif =Tarif(isim,malzeme,byteDizisi)

            //Rxjava
            mDisposable.add(
                tarifDao.insert(tarif)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponseForInsert))




        }
    }

    private fun handleResponseForInsert() {
        //Bir Önceki Fragmenta Don -mesela yani kaydettikten sonra yapılıcak birşey

        val action = TarifFragmentDirections.actionTarifFragmentToListeFragment()
        Navigation.findNavController(requireView()).navigate(action)

    }

    private fun kucukBitmapOlustur(kullanicininSectigiBitmap : Bitmap, maximumBoyut : Int) : Bitmap{
        var width = kullanicininSectigiBitmap.width
        var height = kullanicininSectigiBitmap.height
        val bitmapOrani : Double = width.toDouble() / height.toDouble()

        if (bitmapOrani > 1){
            //gorsel yatay
            width = maximumBoyut
            val kisaltilmisYükseklik = width / bitmapOrani
            height = kisaltilmisYükseklik.toInt()
        }
        else{
            //gorsel dikey
            height = maximumBoyut
            val kisaltilmisGenislik = width * bitmapOrani
            width = kisaltilmisGenislik.toInt()

        }

        return Bitmap.createScaledBitmap(kullanicininSectigiBitmap,width,height,true)
    }

    private fun registerLauncher(){

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK){
            val intentFromResult = result.data
            if (intentFromResult != null){
                secilenGorsel = intentFromResult.data
                try {
                    if(Build.VERSION.SDK_INT >= 28){
                        val source = ImageDecoder.createSource(requireActivity().contentResolver,secilenGorsel!!)
                        secilenBitmap = ImageDecoder.decodeBitmap(source)
                        binding.gorsel.setImageBitmap(secilenBitmap)
                    }
                    else{
                        secilenBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,secilenGorsel)
                        binding.gorsel.setImageBitmap(secilenBitmap)
                    }

                } catch (e: Exception){
                    println(e.localizedMessage)
                }

            }
        }


        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->
            if(result){
                //izin verildi, Galeriye Gidebiliriz
                val intentToGalery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGalery)

            }
            else
            { //izin verilmedi
                Toast.makeText(requireContext(),"İzin Verilmedi",Toast.LENGTH_LONG).show()

            }

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposable.clear()
    }
}