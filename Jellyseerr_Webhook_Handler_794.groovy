/**
 *  Jellyseerr Webhook Handler
 *
 *  Exposes a cloud-accessible webhook endpoint for Jellyseerr.
 *  Receives JSON payloads, extracts the mediaName variable,
 *  and sends a customizable notification using one or more selected devices.
 *
 *  Requirements:
 *   • OAuth enabled
 *   • User selects one or more notification-capable devices
 *   • Customizable notification message template
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
                  title: "Notification message template",
                  description: "Use %mediaName% to include the media title",
                  defaultValue: "Your media \"%mediaName%\" is ready to stream!",
                  required: true
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
        def name = payload.mediaName ?: payload.subject ?: "Unknown"
        log.info "Webhook received: mediaName=${name}"
        
        // Build notification message by replacing placeholder
        def message = messageTemplate.replaceAll("%mediaName%", name)
        
        // Send notification to each selected device
        notificationDevices.each { dev ->
            dev.deviceNotification(message)
            log.info "Notification sent to ${dev.displayName}: ${message}"
        }
        
        return [
            success: true,
            message: "Notifications delivered"
        ]
    } catch (e) {
        log.error "Error processing webhook: ${e}"
        return [
            success: false,
            message: "Error: ${e.message}"
        ]
    }
}
