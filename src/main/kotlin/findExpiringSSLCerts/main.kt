package findExpiringSSLCerts

import software.amazon.awssdk.regions.Region.AWS_GLOBAL
import software.amazon.awssdk.services.route53.Route53Client
import software.amazon.awssdk.services.route53.model.*
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.*
import java.util.*
import java.net.InetSocketAddress
import javax.net.ssl.SSLSocket

val route53ClientInstance: Route53Client = Route53Client.builder().region(AWS_GLOBAL).build()
var listOfTimedOuts : ArrayList<String> = ArrayList()

internal fun main() {
    findAllHostedZones().let { ListOfZones ->
        ListOfZones?.forEach { zones ->
           println("Zone: ${zones.name()}(${zones.id()})")
           getRecordSets(zones.id())?.resourceRecordSets()?.forEach{
               if(it.type() == RRType.A || it.type() == RRType.CNAME)
                   getSSLCertsUsingSockets(it.name().removeSuffix("."))
           }
       }
   }

    println("Terminated ${Date()} \n List of timed out URLs: \n $listOfTimedOuts")
}

private fun getSSLCertsUsingSockets(url: String){
    try {
        val socket = SSLSocketFactory.getDefault().createSocket() as SSLSocket
            socket.soTimeout = 3000
            socket.connect(InetSocketAddress(url, 443), 3000)
            socket.startHandshake()

        for (c in socket.session.peerCertificates) { //For every cert in the chain
            val xc = c as X509Certificate
            val daysLeft = (xc.notAfter.time -  Date().time) / (1000 * 60 * 60 * 24)
            if(daysLeft < 100)
                println("$url certificate expires on : ${xc.notAfter}.. only $daysLeft days to go")
        }
    }catch(e : Exception){
        listOfTimedOuts.add(url)
    }
}

private fun trustManger(): Array<TrustManager> {
    return arrayOf(object : X509TrustManager {
        @Throws(CertificateException::class)
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        @Throws(CertificateException::class)
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }
    })
}

private fun getRecordSets(zoneID : String): ListResourceRecordSetsResponse? {
    val request = ListResourceRecordSetsRequest.builder().hostedZoneId(zoneID).build()
    return route53ClientInstance.listResourceRecordSets(request)
}

private fun findAllHostedZones(): MutableList<HostedZone>? {
    return route53ClientInstance.listHostedZones().hostedZones()
}
