[README.md](https://github.com/user-attachments/files/22933727/README.md)
# Jellyseerr Webhook Handler for Hubitat

This custom Hubitat app exposes a cloud-accessible webhook endpoint for Jellyseerr.
It receives JSON payloads when media becomes available, extracts key variables, and sends customizable notifications via one or more selected notification-capable devices.

## Features

- Cloud webhook endpoint with OAuth security
- Supports multiple notification devices
- Customizable message templates with placeholders:
  - `%mediaName%` – Title of the movie or show
  - `%mediaType%` – "movie" or "tv"
  - `%description%` – Synopsis/overview of the media
  - `%notificationType%` – Notification event type (e.g., `MEDIA_AVAILABLE`)
  - `%requestedBy%` – Username of the requester

## Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/jellyseerr-hubitat-webhook.git
   ```
2. **Add App Code to Hubitat**:
   - Open your Hubitat web interface.
   - Navigate to **Apps Code** → **Add New App**.
   - Paste the contents of `JellyseerrWebhookHandler.groovy`.
   - Click **Save**.
3. **Enable OAuth**:
   - In the code editor, click **OAuth** then **Update** to generate an access token.
4. **Install the App**:
   - Go to **Apps** → **Add User App**.
   - Select **Jellyseerr Webhook Handler** and click **Done**.

## Configuration

1. **Webhook Endpoint**:
   - In the app settings, copy the **Cloud Webhook URL**:
     ```text
     https://cloud.hubitat.com/api/<HUB-UID>/apps/<APP-ID>/webhook?access_token=<TOKEN>
     ```
2. **Notification Settings**:
   - Select one or more devices with the **Notification** capability.
   - Customize the **Message Template** using the placeholders above.

## Jellyseerr Setup

1. In Jellyseerr, navigate to **Settings** → **Notifications** → **Webhook**.
2. Enable the webhook and paste the **Cloud Webhook URL**.
3. Use the following JSON payload for **Media Available** notifications:
   ```json
   {
     "mediaName": "{{subject}}",
     "mediaType": "{{media_type}}",
     "description": "{{message}}",
     "notificationType": "{{notification_type}}",
     "requestedBy": "{{requestedBy_username}}"
   }
   ```
4. Test the webhook and verify Hubitat logs.

## Usage

- When media is available, Jellyseerr will POST to the Hubitat endpoint.
- The app extracts variables from the payload, constructs the notification message, and sends it to your selected devices.
- Check **Logs** in Hubitat for confirmation and troubleshooting.

## License

MIT License

## Contributing

Contributions are welcome! Please open issues or submit pull requests on GitHub.
