class Model {
    private var views: ArrayList<IView> = ArrayList()

    fun addView(view: IView) {
        views.add(view)
        views.updateView()
    }

    private fun notifyObservers() {
        for (view in views) {
            view.updateView()
        }
    }
}