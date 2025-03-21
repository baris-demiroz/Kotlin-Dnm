package com.example.yemekkitabi.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.yemekkitabi.adapter.TarifAdapter
import com.example.yemekkitabi.databinding.FragmentListeBinding
import com.example.yemekkitabi.model.Tarif
import com.example.yemekkitabi.roomdb.TarifDao
import com.example.yemekkitabi.roomdb.TarifDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


class ListeFragment : Fragment() {
    private var _binding: FragmentListeBinding? = null
    private val binding get() = _binding!!
    private val mDisposable = CompositeDisposable()

    private lateinit var db : TarifDatabase
    private lateinit var  tarifDao : TarifDao


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(requireContext(),TarifDatabase::class.java,"Tarifler").build()
        tarifDao = db.tarifDao()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnEkle.setOnClickListener{
            ekle(it)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        verileriAl()
    }

    private fun verileriAl(){
    mDisposable.add(
        tarifDao.getAll()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::handeResponse)
    )
    }

    private fun handeResponse(tarifler : List<Tarif>){
        val adapter = TarifAdapter(tarifler)
        binding.recyclerView.adapter = adapter

    }

    fun ekle(view: View){
        val action = ListeFragmentDirections.actionListeFragmentToTarifFragment(bilgi = "",id =-1)
        Navigation.findNavController(view).navigate(action)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposable.clear()
    }
}