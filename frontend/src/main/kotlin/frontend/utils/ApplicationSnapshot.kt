package frontend.utils

import models.Board
import models.Item

data class ApplicationSnapshot(
    val action: Actions,
    val items: List<Item>,
    val boards: List<Board>,
    val actionMetaData: ActionMetaData?
)