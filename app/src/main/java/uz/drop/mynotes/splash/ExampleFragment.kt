package uz.drop.mynotes.splash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.example_fragment.*
import uz.drop.mynotes.R

class ExampleFragment : Fragment(R.layout.example_fragment) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle = requireArguments()
        val data = bundle.getSerializable("data") as Data
        container.setBackgroundColor(data.color)
        featureText.text = data.feature
        image.setImageResource(data.image)
        descriptionLayout.text = data.description
        val pos=bundle.getInt("pos",0)
        if (pos==2) continueBtn.visibility=View.VISIBLE
        val communicator=requireActivity() as? Communicator
        continueBtn.setOnClickListener {
            communicator?.onClick()
        }
    }
}

interface Communicator{
    fun onClick()
}