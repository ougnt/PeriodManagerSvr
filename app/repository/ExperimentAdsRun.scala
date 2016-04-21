package repository

import java.lang.reflect.Field

import context.CoreContext
import repositories.InjectAble

/**
  * * # Created by wacharint on 4/21/16.
  **/
class ExperimentAdsRun(implicit context: CoreContext) extends InjectAble {

  var experimentRunId: Int = 0
  var experimentId: Int = 0
  var displayedLanguage = ""
  var aAdsUrl  = ""
  var aAdsShow: Int = 0
  var aAdsClick: Int = 0
  var bAdsUrl = ""
  var bAdsShow: Int = 0
  var bAdsClick: Int = 0
  var cAdsUrl = ""
  var cAdsShow: Int = 0
  var cAdsClick: Int = 0
  var dAdsUrl = ""
  var dAdsShow: Int = 0
  var dAdsClick: Int = 0
  var eAdsUrl = ""
  var eAdsShow: Int = 0
  var eAdsClick: Int = 0
  var fAdsUrl = ""
  var fAdsShow: Int = 0
  var fAdsClick: Int = 0

  recStatus = 1

  override val callContext: CoreContext = context
  override val tableName: String = "experiment_ads_run"
  override var fields: Seq[Field] = classOf[ExperimentAdsRun].getDeclaredFields
}