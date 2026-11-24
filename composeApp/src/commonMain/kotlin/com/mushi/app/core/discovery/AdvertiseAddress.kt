package com.mushi.app.core.discovery

/**
 * The LAN address to publish in our mDNS advertisement, or null to let the platform decide.
 *
 * Only the desktop needs this: there the dns-sd library otherwise grabs the first site-local
 * address of *any* interface, which on a dev box is often a Docker/VM bridge (e.g. minikube's
 * 192.168.49.1) that other devices can't reach.
 */
internal expect fun preferredAdvertiseAddress(): String?
