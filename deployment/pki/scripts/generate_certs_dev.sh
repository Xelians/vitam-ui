#!/usr/bin/env bash
set -e

######################################################################
############################# Includes  ##############################
######################################################################

. "$(dirname $0)/lib/certs.sh"

######################################################################
#########################    Overriding    ###########################
######################################################################

REPERTOIRE_ROOT="$( cd "$( readlink -f $(dirname ${BASH_SOURCE[0]}) )/../../../dev-deployment" ; pwd )"

function getHostCertificateCn {
    echo "dev.vitamui.com"
}

function generateCerts {

    # Copy CA
    pki_logger "Recopie des clés publiques des CA"
    copyCAFromPki client-external
    copyCAFromPki client-vitam
    copyCAFromPki server

    # Generate hosts certificates
    pki_logger "Génération des certificats serveurs"
    # Zone interne
    generateHostCertAndStorePassphrase          security-internal   hosts_vitamui_security_internal
    generateHostCertAndStorePassphrase          iam-internal        hosts_vitamui_iam_internal
    generateHostCertAndStorePassphrase          referential-internal        hosts_vitamui_referential_internal
    #Zone externe
    generateHostCertAndStorePassphrase          iam-external        hosts_vitamui_iam_external
    generateHostCertAndStorePassphrase          cas-server          hosts_cas_server
    generateHostCertAndStorePassphrase          referential-external        hosts_vitamui_referential_external
    #Zone UI
    generateHostCertAndStorePassphrase          ui-portal           hosts_ui_portal
    generateHostCertAndStorePassphrase          ui-identity         hosts_ui_identity
    generateHostCertAndStorePassphrase          ui-identity-admin   hosts_ui_identity_admin
    generateHostCertAndStorePassphrase          ui-referential      hosts_ui_referential
    #Reverse
    generateHostCertAndStorePassphrase          reverse             hosts_vitamui_reverseproxy

    # Example of generated client cert for a customer allowing to perform request on external APIs
    generateClientCertAndStorePassphrase        customer_x          client-external

    # Generate Vitam certificates for VitamUI
    generateClientCertAndStorePassphrase        vitamui             client-vitam
}

######################################################################
#############################    Main    #############################
######################################################################

main "$@"