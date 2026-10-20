package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

data class YouTubeDiagnosticReport(
    val youtubeId: String,
    val embedUrl: String,
    val originParam: String,
    val referrerHeader: String,
    val isIdValid: Boolean,
    val isOriginValid: Boolean,
    val isReferrerValid: Boolean,
    val errorCode: Int?,
    val isRendererCrashed: Boolean,
    val isPlayerReady: Boolean,
    val probableCause: String,
    val recommendedAction: String
)

object YouTubeDiagnosticAnalyzer {
    fun analyze(
        youtubeId: String,
        packageName: String = "com.example",
        errorCode: Int?,
        isRendererCrashed: Boolean,
        isPlayerReady: Boolean
    ): YouTubeDiagnosticReport {
        val embedUrl = "https://www.youtube.com/embed/$youtubeId?playsinline=1&rel=0&modestbranding=1&enablejsapi=1&origin=https://$packageName"
        val originParam = "https://$packageName"
        val referrerHeader = "https://$packageName"
        val isIdValid = youtubeId.matches(Regex("^[a-zA-Z0-9_-]{11}$"))
        val isOriginValid = originParam.startsWith("https://")
        val isReferrerValid = referrerHeader.startsWith("https://")

        val (cause, action) = when {
            isRendererCrashed -> {
                "Arrêt du processus de rendu Chromium (GPU Mesa) : Le moteur de rendu de l'environnement virtuel n'a pas pu allouer de décodeur graphique matériel." to
                        "Utilisez le bouton 'Ouvrir dans YouTube' pour lancer la vidéo dans l'application officielle YouTube ou le navigateur système."
            }
            !isIdValid -> {
                "Identifiant YouTube invalide : '$youtubeId' ne respecte pas le format standard YouTube de 11 caractères alphanumériques." to
                        "Vérifiez l'URL de la vidéo et assurez-vous que l'identifiant est correct."
            }
            errorCode == 150 || errorCode == 101 -> {
                "Restriction d'intégration (Code $errorCode) : Le propriétaire de cette vidéo a désactivé sa lecture sur les sites ou applications externes." to
                        "Cette vidéo ne peut être lue que directement sur la plateforme YouTube. Appuyez sur 'Ouvrir dans YouTube' pour la visionner."
            }
            errorCode == 100 -> {
                "Vidéo introuvable ou privée (Code 100) : La vidéo a été supprimée, désactivée ou est restreinte à un accès privé." to
                        "Vérifiez que la vidéo est toujours en ligne sur YouTube ou choisissez une autre vidéo."
            }
            errorCode == 2 -> {
                "Paramètre de requête invalide (Code 2) : L'identifiant de vidéo ou un paramètre URL n'est pas reconnu par l'API YouTube." to
                        "Vérifiez la syntaxe de l'identifiant YouTube."
            }
            errorCode == 5 -> {
                "Erreur lecteur HTML5 (Code 5) : Le lecteur YouTube HTML5 n'a pas pu s'initialiser dans ce composant WebView." to
                        "Relancez le lecteur ou basculez vers l'application YouTube externe."
            }
            errorCode == 152 -> {
                "Conflit de domaine ou de sécurité (Code 152) : Le paramètre 'origin' ($originParam) ou le 'referrer' ($referrerHeader) n'a pas été accepté par le serveur YouTube." to
                        "L'origine et le referrer sont actuellement alignés sur 'https://$packageName' pour résoudre ce blocage."
            }
            !isPlayerReady -> {
                "En attente de connexion ou blocage réseau : L'iframe YouTube est en cours de négociation avec les serveurs Google." to
                        "Vérifiez la connexion Internet. Si la vidéo ne démarre pas après quelques secondes, le flux peut être restreint par YouTube."
            }
            else -> {
                "Configuration conforme : L'URL d'intégration, le paramètre d'origine ($originParam) et le referrer ($referrerHeader) sont valides." to
                        "Le lecteur est prêt. Appuyez sur l'icône de lecture au centre pour démarrer le visionnage."
            }
        }

        return YouTubeDiagnosticReport(
            youtubeId = youtubeId,
            embedUrl = embedUrl,
            originParam = originParam,
            referrerHeader = referrerHeader,
            isIdValid = isIdValid,
            isOriginValid = isOriginValid,
            isReferrerValid = isReferrerValid,
            errorCode = errorCode,
            isRendererCrashed = isRendererCrashed,
            isPlayerReady = isPlayerReady,
            probableCause = cause,
            recommendedAction = action
        )
    }
}

@Composable
fun YouTubeDiagnosticDialog(
    report: YouTubeDiagnosticReport,
    onDismiss: () -> Unit,
    onOpenExternal: () -> Unit
) {
    val context = LocalContext.current
    val hasIssue = report.isRendererCrashed || !report.isIdValid || (report.errorCode != null && report.errorCode != 0)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .testTag("youtube_diagnostic_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // En-tête
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (hasIssue) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (hasIssue) Icons.Default.Warning else Icons.Default.Analytics,
                                    contentDescription = null,
                                    tint = if (hasIssue) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Diagnostic Lecteur YouTube",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (hasIssue) "Problème détecté" else "Configuration optimale",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (hasIssue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("diagnostic_close_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bannière cause probable
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (hasIssue) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = if (hasIssue) Icons.Default.Error else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (hasIssue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cause probable :",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (hasIssue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = report.probableCause,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Action recommandée :",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = report.recommendedAction,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Détails techniques des paramètres
                Text(
                    text = "Vérification des paramètres du lecteur",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 1. Identifiant vidéo
                DiagnosticItem(
                    label = "ID Vidéo YouTube",
                    value = report.youtubeId,
                    isValid = report.isIdValid,
                    statusText = if (report.isIdValid) "11 caractères (Valide)" else "Format invalide"
                )

                // 2. Paramètre origin
                DiagnosticItem(
                    label = "Paramètre d'origine (origin)",
                    value = report.originParam,
                    isValid = report.isOriginValid,
                    statusText = if (report.isOriginValid) "Origine HTTPS standard" else "Origine incorrecte"
                )

                // 3. Referrer HTTP
                DiagnosticItem(
                    label = "En-tête Referrer",
                    value = report.referrerHeader,
                    isValid = report.isReferrerValid,
                    statusText = if (report.isReferrerValid) "Base URL youtube.com transmise" else "Referrer absent"
                )

                // 4. URL d'intégration iframe
                DiagnosticItem(
                    label = "URL d'intégration (Embed)",
                    value = report.embedUrl,
                    isValid = report.isIdValid,
                    statusText = "IFrame API + playsinline"
                )

                // 5. Code d'erreur IFrame renvoyé
                DiagnosticItem(
                    label = "Code d'erreur IFrame reçu",
                    value = when (report.errorCode) {
                        null -> "Aucune erreur signalée (Lecteur actif)"
                        150, 101 -> "Erreur ${report.errorCode} : Intégration désactivée par l'auteur"
                        100 -> "Erreur 100 : Vidéo introuvable / privée"
                        2 -> "Erreur 2 : Paramètre URL invalide"
                        5 -> "Erreur 5 : Échec HTML5"
                        152 -> "Erreur 152 : Conflit de sécurité d'origine"
                        else -> "Erreur ${report.errorCode}"
                    },
                    isValid = report.errorCode == null || report.errorCode == 0,
                    statusText = if (report.errorCode == null) "OK" else "Erreur ${report.errorCode}"
                )

                // 6. État du moteur Chromium
                DiagnosticItem(
                    label = "Processus de rendu WebView",
                    value = if (report.isRendererCrashed) "Arrêté (Conflit GPU conteneur)" else "Opérationnel",
                    isValid = !report.isRendererCrashed,
                    statusText = if (report.isRendererCrashed) "Crash code -1" else "Actif"
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Boutons d'action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText(
                                "Diagnostic YouTube",
                                """
                                Diagnostic Lecteur YouTube:
                                ID: ${report.youtubeId}
                                Embed URL: ${report.embedUrl}
                                Origin: ${report.originParam}
                                Referrer: ${report.referrerHeader}
                                Code Erreur: ${report.errorCode ?: "Aucun"}
                                Moteur Crashé: ${report.isRendererCrashed}
                                Cause: ${report.probableCause}
                                Action: ${report.recommendedAction}
                                """.trimIndent()
                            )
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Rapport de diagnostic copié", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("diagnostic_copy_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copier", maxLines = 1)
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            onOpenExternal()
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("diagnostic_open_youtube_button")
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ouvrir YouTube", maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagnosticItem(
    label: String,
    value: String,
    isValid: Boolean,
    statusText: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isValid) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = if (isValid) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
