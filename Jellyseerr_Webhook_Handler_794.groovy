/**
 *  Jellyseerr Webhook Handler
 *
 *  Exposes a cloud-accessible webhook endpoint for Jellyseerr.
 *  Receives JSON payloads, extracts variables, and sends a customizable notification
 *  via one or more selected notification-capable devices.
 *
 *  Variables available in payload:
 *    • mediaName         = The title of the movie or show
 *    • mediaType         = “movie” or “tv”
 *    • description       = The media’s overview or synopsis
 *    • notificationType  = The type of notification (e.g., MEDIA_AVAILABLE)
 *    • requestedBy       = Username of who requested the media
 *
 *  Users may include these variables in the message template:
 *    %mediaName%, %mediaType%, %description%, %notificationType%, %requestedBy%
 */
definition(
    name: "Jellyseerr Webhook Handler",
    namespace: "custom",
    author: "Colin Ho",
    description: "Receives Jellyseerr webhook and sends custom notifications",
    category: "Convenience",
    oauth: true,
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
    importUrl: "" // Optional GitHub raw URL
)

preferences {
    page(name: "mainPage")
}

def mainPage() {
    dynamicPage(name: "mainPage", install: true, uninstall: true) {
        section("Webhook Endpoint") {
            if (!state.accessToken) {
                createAccessToken()
            }
            paragraph "Cloud Webhook URL:"
            paragraph "https://cloud.hubitat.com/api/${getHubUID()}/apps/${app.id}/webhook?access_token=${state.accessToken}"
        }
        section("Notification Settings") {
            input name: "notificationDevices",
                  type: "capability.notification",
                  title: "Select notification device(s)",
                  required: true,
                  multiple: true
            input name: "messageTemplate",
                  type: "text",
                  title: "Notification message",                
                  defaultValue: "Your media \"%mediaName%\" is ready to stream!",
                  required: true
                  paragraph """
                    Available variables:
                    • %mediaName% — Title of the movie or show  
                    • %mediaType% — “movie” or “tv”  
                    • %description% — Synopsis of the media  
                    • %notificationType% — Event type (e.g., MEDIA_AVAILABLE)  
                    • %requestedBy% — Username of the requester  
                    """
        }
    }
}

mappings {
    path("/webhook") {
        action: [ POST: "handleWebhook" ]
    }
}

def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
    if (!state.accessToken) {
        createAccessToken()
    }
    log.info "Jellyseerr Webhook Handler initialized"
}

def handleWebhook() {
    try {
        def payload = request.JSON
        // Extract all variables with safe defaults
        def mediaName        = payload.mediaName        ?: "Unknown"
        def mediaType        = payload.mediaType        ?: "Unknown"
        def description      = payload.description      ?: ""
        def notificationType = payload.notificationType ?: ""
        def requestedBy      = payload.requestedBy      ?: ""
        
        log.info "Webhook received: mediaName=${mediaName}, mediaType=${mediaType}, requestedBy=${requestedBy}"
        
        // Replace placeholders in template
        def message = messageTemplate
            .replaceAll("%mediaName%", mediaName)
            .replaceAll("%mediaType%", mediaType)
            .replaceAll("%description%", description)
            .replaceAll("%notificationType%", notificationType)
            .replaceAll("%requestedBy%", requestedBy)
        
        // Send to each selected device
        notificationDevices.each { dev ->
            dev.deviceNotification(message)
            log.info "Notification sent to ${dev.displayName}: ${message}"
        }
        
        return [ success: true, message: "Notifications delivered" ]
    } catch (e) {
        log.error "Error processing webhook: ${e}"
        return [ success: false, message: "Error: ${e.message}" ]
    }
}
