package com.rockthevote.grommet.data.db.relationship

import androidx.room.Embedded
import androidx.room.Relation
import com.rockthevote.grommet.data.db.model.PartnerInfo
import com.rockthevote.grommet.data.db.model.Session

/**
 * Created by Mechanical Man on 5/3/20.
 *
 * inverse relationship to make observing the partner info content associated with a given session
 * more straight forward.
 */
data class SessionWithPartnerInfo(
        @Embedded val session: Session?,
        @Relation(
                parentColumn = "partner_info_id",
                entityColumn = "partner_info_id"
        )
        val partnerInfo: PartnerInfo
)