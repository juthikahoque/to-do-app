package frontend.interfaces

// This interface enables views to be observers of the model
interface IView {

    // Each IView must implement a function to update its view after
    // receiving an update from the model
    fun updateView()
}
