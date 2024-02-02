// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/Emran/Developer/Glasgow-University/TeamProject/cardgame/conf/routes
// @DATE:Fri Feb 02 14:23:38 GMT 2024


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
