package com.rockthevote.grommet.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.rockthevote.grommet.data.db.model.PartnerInfo
import com.rockthevote.grommet.data.db.relationship.PartnerInfoWithSessions

/**
 * Created by Mechanical Man on 4/18/20.
 */
@Dao
interface PartnerInfoDao {
    @Query("SELECT * FROM partner_info")
    fun getAll(): List<PartnerInfo>

    @Transaction
    @Query("SELECT * FROM partner_info")
    fun getPartnerInfoWithSessions(): List<PartnerInfoWithSessions>
}