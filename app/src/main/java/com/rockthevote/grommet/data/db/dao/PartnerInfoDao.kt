package com.rockthevote.grommet.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.rockthevote.grommet.data.db.model.PartnerInfo

/**
 * Created by Mechanical Man on 4/18/20.
 */
@Dao
interface PartnerInfoDao {
    @Query("SELECT * FROM partner_info")
    fun getAll(): List<PartnerInfo>
}