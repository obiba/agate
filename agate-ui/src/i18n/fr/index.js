export default {
  auth: {
    signout: 'Déconnexion',
  },
  main: {
    powered_by: 'Propulsé par',
  },
  application: {
    add_realm_groups: 'Ajouter un domaine',
    add_scope: 'Ajouter une permission',
    add: 'Ajouter une application',
    auto_approval_hint: "Approuver automatiquement un usager qui s'est inscrit via l'application. Sinon, l'usager sera dans l'état \"En attente\", nécessitant une approbation manuelle.",
    auto_approval: "Usager approuvé à l'inscription",
    copy_key: 'Copier la clé',
    edit: "Modifier l'application",
    generate_key: 'Générer une clé',
    key_copied: 'Clé copiée',
    key_hint_edit: "Laisser vide pour ne pas modifier la clé secrète de l'application.",
    key_hint: "Cette clé est utilisée pour authentifier l'application avec l'API.",
    key_min_length: 'La clé doit comporter au moins {min} caractères',
    key_required: 'La clé est requise',
    key: 'Clé',
    realms_groups_hint: "Mapping entre des domaines et des groupes. Lorsque défini, les domaines correspondants seront proposés pour la connexion/inscription de l'usager à partir de l'application. Lorsqu'un usager rejoint une application, en utilisant un domaine, le(s) groupe(s) correspondant(s) seront automatiquement appliqués.",
    realms_groups: 'Domaines et groupes',
    redirect_uris_hint: "URL de rappel vers le serveur de l'application, requis dans le contexte OAuth. Utilisez des virgules pour séparer plusieurs URLs de rappel autorisées.",
    redirect_uris: 'URLs de redirection',
    remove_confirm: "Veuillez confirmer la suppression de l'application: {name}",
    remove: "Suppression de l'application",
    save_failed: "Échec de l'enregistrement de l'application",
    saved: 'Application enregistrée',
    scopes_hint: "Les permissions permettent de qualifier l'accès d'autorisation à l'application qui est accordé dans le contexte OAuth. Les permissions sont optionnelles.",
    scopes: 'Permissions',
  },
  group: {
    add: 'Ajouter un groupe',
    applications_hint: "Les membres d'un groupe ont accès aux applications associées à ce groupe.",
    edit: 'Modifier le groupe',
    remove_confirm: 'Veuillez confirmer la suppression du groupe: {name}. Les usagers membres de ce groupe ne seront pas affectés.',
    remove: 'Suppression du groupe',
    save_failed: "Échec de l'enregistrement du groupe",
    saved: 'Groupe enregistré',
  },
  user: {
    add: 'Ajouter un usager',
    applications_hint: 'Les usagers peuvent être directement accordés un accès aux applications.',
    approve_error: "Échec de l'approbation de l'usager",
    approve: "Approuver l'usager",
    approved: 'Usager approuvé',
    copy_password: 'Copier le mot de passe',
    disable_2fa: 'Désactiver le 2FA',
    edit: "Modifier l'usager",
    firstName: 'Prénom',
    generate_password: 'Générer un mot de passe',
    groups_hint: 'Les usagers peuvent être membres de groupes pour accéder aux applications associées à ces groupes.',
    language: 'Langue',
    lastName: 'Nom de famille',
    otp_disable_error: 'Échec de la désactivation du 2FA',
    otp_disabled: '2FA désactivé',
    password_copied: 'Mot de passe copié',
    password_hint: "{'Le mot de passe doit contenir au moins un chiffre, une lettre majuscule, une lettre minuscule, un caractère spécial (qui inclut @#$%^&+=!) et aucun espace blanc.'}",
    password_min_length: 'Le mot de passe doit comporter au moins {min} caractères',
    password_required: 'Le mot de passe est requis',
    password: 'Mot de passe',
    realm_hint: "Domaine dans lequel l'usager est authentifié.",
    reject_confirm: "Veuillez confirmer le rejet de l'usager: {name}",
    reject: "Rejeter l'usager",
    remove_confirm: "Veuillez confirmer la suppression de l'usager: {name}",
    remove_error: "Échec de la suppression de l'usager",
    remove: "Suppression de l'usager",
    removed: 'Usager supprimé',
    reset_password_confirm: 'Veuillez confirmer que vous souhaitez envoyer une notification de réinitialisation du mot de passe à cet usager.',
    reset_password_error: "Échec de la réinitialisation du mot de passe de l'usager",
    reset_password_success: 'Notification de réinitialisation du mot de passe envoyée',
    reset_password: 'Réinitialiser le mot de passe',
    role: {
      'agate-user': 'Usager',
      'agate-administrator': 'Administrateur',
    },
    role_hint: {
      'agate-user': 'Un usager simple peut uniquement accéder à son propre compte.',
      'agate-administrator': 'Un administrateur peut gérer tous les usagers, les groupes, les applcations, les domaines etc.',
    },
    saved: 'Usager enregistré',
    save_failed: "Échec de l'enregistrement de l'usager",
    show_password: 'Afficher le mot de passe',
    status: {
      ACTIVE: 'Actif',
      APPROVED: 'Approuvé',
      INACTIVE: 'Inactif',
      PENDING: 'En attente',
    },
    status_hint: {
      ACTIVE: 'Usager opérationnel.',
      APPROVED: "Usager approuvé par l'administrateur mais confirmation de l'usager requise (courriel, réinitialisation du mot de passe).",
      INACTIVE: 'Usager désactivé.',
      PENDING: "Usager en attente d'approbation.",
    },
    update_password: 'Mettre à jour le mot de passe',
    attributes: {
      title: 'Attributs utilisateur',
      hint: "Informations additionnelles sur l'usager.",
      add: "Ajouter un attribut d'utilisateur",
      update: "Mettre à Jour l'attribut d'utilisateur",
      updated: 'Attribut utilisateur mis à jour avec succès',
      update_failed: "Échec de la mise à jour de l'attribut utilisateur",
      remove: "Supprimer l'attribut utilisateur",
      remove_confirm: "Veuillez confirmer la suppression de l'attribut utilisateur : {name}",
      name_exists: "Le nom de l'attribut utilisateur existe déjà",
    },
  },
  realm: {
    activate: 'Activer',
    add: 'Ajouter un domaine',
    deactivate: 'Désactiver',
    edit: 'Modifier le domaine',
    for_signup: 'Pour inscription',
    groups_hint: "Les usagers s'inscrivant via ce domaine seront automatiquement ajoutés aux groupes sélectionnés.",
    public_url: 'URL publique',
    public_url_hint: 'URL de base publique du domaine qui sera utilisée pour les liens dans les courriels de notification et les redirections OpenID Connect.',
    realm: 'Domaine',
    remove_confirm: 'Veuillez confirmer la suppression du domaine: {name}. Les applications, les groupes et les usagers associés à ce domaine ne seront pas supprimés.',
    remove: 'Suppression du domaine',
    saved: 'Domaine enregistré',
    save_failed: "Échec de l'enregistrement du domaine",
    sso_domain: 'Domaine SSO',
    sso_domain_hint: 'Domaine de connexion unique.',
    title: 'Titre',
    title_hint: "Le titre du domaine tel qu'il sera affiché à l'utilisateur dans la page d'identification.",
    user_count: "Nombre d'usagers",
    status: {
      ACTIVE: 'Actif',
      INACTIVE: 'Inactif',
    },
    type: {
      'agate-ad-realm': 'Active Directory',
      'agate-jdbc-realm': 'Base de données SQL',
      'agate-ldap-realm': 'LDAP',
      'agate-oidc-realm': 'OpenID Connect',
    },
    oidc: {
      title: 'OpenID Connect',
      client_id: 'ID client',
      client_id_hint: 'ID client fourni par le fournisseur OpenID Connect.',
      client_secret: 'Secret client',
      client_secret_hint: 'Secret client fourni par le fournisseur OpenID Connect.',
      discovery_uri: 'URI de découverte',
      discovery_uri_hint: 'URI de découverte OpenID Connect.',
      account_url: 'URL du compte',
      account_url_hint: "Lien pour accéder au compte de l'usager.",
      scope: 'Scope',
      scope_hint: 'Liste des scopes à spécifier pour récupérer des informations utilisateur. Habituellement, openid suffit.',
      groups_claim: 'Groupes par champ',
      groups_claim_hint: "Champ de UserInfo duquel seront extraits les noms des groupes. Ignoré si 'Groupes par JS' est défini.",
      groups_js: 'Groupes par JS',
      groups_js_hint: 'Code javascript pour extraire les noms des groupes de UserInfo.',
      nonce: 'Utiliser un nonce',
      connect_timeout: 'Délai de connexion',
      connect_timeout_hint: 'Délai maximum en millisecondes pour établir une connexion. Zero implique pas de délai.',
      read_timeout: 'Délai de lecture',
      read_timeout_hint: 'Délai maximum en millisecondes pour lire les données. Zero implique pas de délai.',
    },
    ldap: {
      title: 'LDAP',
      url: 'URL',
      url_hint: 'URL de connexion au serveur LDAP.',
      system_username: "Nom d'usager système",
      system_password: 'Mot de passe système',
      user_dn_template: 'Modèle de DN utilisateur',
      user_dn_template_hint: 'Modèle utilisé pour rechercher un utilisateur dans le serveur LDAP.',
    },
    ad: {
      title: 'Active Directory',
      url: 'URL',
      url_hint: 'URL de connexion au serveur Active Directory.',
      system_username: "Nom d'usager système",
      system_password: 'Mot de passe système',
      search_filter: 'Filtre de recherche',
      search_filter_hint: 'Filtre utilisé pour rechercher un utilisateur dans le serveur Active Directory.',
      search_base: 'Base de recherche',
      search_base_hint: 'Base utilisée pour rechercher un utilisateur dans la hiérarchie Active Directory.',
      principal_suffix: 'Suffixe du principal',
      principal_suffix_hint: "Suffixe utilisé pour construire le nom principal de l'utilisateur.",
    },
    jdbc: {
      title: 'Base de données SQL',
      url: 'URL',
      url_hint: 'URL de connexion à la base de données SQL.',
      username: "Nom d'usager",
      password: 'Mot de passe',
      auth_query: "Requête d'authentification",
      auth_query_hint: "Requête utilisée pour extraire le mot de passe de l'utilisateur.",
      auth_query_salt_column_hint: 'Assurez-vous d\'inclure le sel dans la requête, e.g. "SELECT password, password_salt FROM users WHERE username = ?"',
      salt_style: 'Style de sel',
      salt_style_hint: 'Style de sel utilisé pour hasher le mot de passe.',
      external_salt: 'Sel externe',
      external_salt_hint: "Sel externe utilisé pour hasher le mot de passe. Si non défini, le nom de l'utilisateur sera utilisé comme sel.",
      algorithm_name: "Nom de l'algorithme",
      algorithm_name_hint: "Nom de l'algorithme utilisé pour hasher le mot de passe (par exemple, SHA-256). Laisser vide si aucun n'est utilisé.",
    },
    mappings: {
      agate_key: 'Agate key',
      title: 'Mappings',
      hint: "Mapping entre les champs de l'usager dans le profile Agate et les champs de UserInfo du domaine.",
      email: 'Courriel',
      email_hint: "Champ de UserInfo duquel sera extrait le courriel de l'usager.",
      firstname: 'Prénom',
      firstname_hint: "Champ de UserInfo duquel sera extrait le prénom de l'usager.",
      lastname: 'Nom de famille',
      lastname_hint: "Champ de UserInfo duquel sera extrait le nom de famille de l'usager.",
      provider_key: 'Provider key',
      username: "Nom d'usager",
      username_hint: 'Champ de UserInfo duquel sera extrait le nom d\'usager de l\'usager. Si non trouvé, les champs suivants évalués dans l\'ordre: "preferred_username", "username", "email", "name" et "sub".',
    },
  },
  system: {
    inactive_timeout_hint: "Délai d'expiration du compte utilisateur en jours.",
    inactive_timeout: "Délai d'expiration inactif (jours)",
    languages_hint: 'Langues possibles pour les courriels de notification.',
    languages: 'Langues',
    long_timeout_hint: 'Délai d\'expiration du ticket en heures lorsque l\'option "se souvenir de moi" est sélectionnée.',
    long_timeout: 'Délai long (heures)',
    name_hint: 'Le nom de votre organisation.',
    otp_strategy_hint: "Forcer l'activation du 2FA pour tous les usagers (selon la stratégie le code temporaire sera envoyé par courriel ou en utilisant une app mobile d'authentification).",
    otp_strategy: 'Stratégie 2FA',
    portal_url_hint: "URL de base publique du portail de l'organisation.",
    portal_url: 'URL du portail',
    public_url_hint: "URL de base publique du portail de l'organisation qui sera utilisée pour les liens dans les courriels de notification et les redirections OpenID Connect.",
    public_url: 'URL publique',
    short_timeout_hint: "Délai d'expiration du ticket en heures.",
    short_timeout: 'Délai court (heures)',
    signup_blacklist_hint: "Les usagers autorisés à s'inscrire ne doivent pas avoir d'adresse courriel dans les domaines de la liste noire.",
    signup_blacklist_hint_form: "Les usagers autorisés à s'inscrire ne doivent pas avoir d'adresse courriel dans les domaines de la liste noire. Utilisez des virgules ou des espaces pour séparer plusieurs domaines.",
    signup_blacklist: "Liste noire d'inscription",
    signup_enabled_hint: "Permettre aux usagers de s'inscrire directement par Agate. Ceci n'affect pas le service offert aux applications.",
    signup_enabled: 'Inscription activée',
    signup_username_hint: "Permettre aux usagers de choisir leur nom d'usager lors de l'inscription, sinon le courriel sera utilisé.",
    signup_username: "Inscription avec nom d'usager",
    signup_whitelist_hint: "Les usagers autorisés à s'inscrire doivent avoir une adresse courriel dans les domaines de la liste blanche.",
    signup_whitelist_hint_form: "Les usagers autorisés à s'inscrire doivent avoir une adresse courriel dans les domaines de la liste blanche. Utilisez des virgules ou des espaces pour séparer plusieurs domaines.",
    signup_whitelist: "Liste blanche d'inscription",
    sso_domain_hint: 'Domaine de connexion unique.',
    sso_domain: 'Domaine SSO',
    translations: {
      title: 'Traductions personnalisés',
      hint: 'Remplacer les traductions par défaut ou en ajouter de nouvelles',
      name_exists: "La clé de traduction existe déjà",
      add: 'Ajouter une traduction',
      add_hint: 'Ajouter la traduction en \'{language}\' - autres langues modifiables dans Paramètres',
      remove_confirm: 'Veuillez confirmer la suppression de la traduction | Veuillez confirmer la suppression des {count} traductions',
      save_changes: 'Sauvegarder les modifications pour éviter de perdre vos ajouts',
    },
    otp_strategies: {
      NONE: 'Aucun',
      APP: 'Application mobile',
      ANY: 'Courriel ou application mobile',
    },
    attributes: {
      title: 'Attributs utilisateur personnalisés',
      hint: 'Ajoutez des attributs personnalisés au profil utilisateur et localisez-les avec des clés préfixées par \'user-info.\' dans les traductions personnalisées (par ex. : \'user-info.institut\').',
      values_hint: 'Valeurs séparées par des virgules',
      add: 'Ajouter un Attribut',
      update: "Mettre à Jour l'Attribut",
      updated: 'Attribut mis à jour',
      update_failed: "Échec de la mise à jour de l'attribut",
      remove: "Supprimer l'Attribut",
      remove_confirm: "Veuillez confirmer la suppression de l'attribut : {name}",
      name_exists: "Le nom de l'attribut existe déjà",
      types: {
        STRING: 'Chaîne de caractères',
        NUMBER: 'Nombre',
        BOOLEAN: 'Booléen',
        INTEGER: 'Entier',
      },
    },
  },
  ticket: {
    events: 'Événements',
    expires: 'Expire',
    login_app: 'Application de connexion',
    remove: 'Supprimer le ticket',
    remove_confirm: 'Veuillez confirmer la suppression du ticket: {id} de {username}',
  },
  add: 'ajouter',
  administration: 'Administration',
  applications_caption: "Gérer les applications, les identifications et les fournisseurs d'identité",
  applications: 'Applications',
  apply: 'Appliquer',
  cancel: 'Annuler',
  content_management: 'Gestion de contenu',
  confirm: 'Confirmer',
  delete: 'Supprimer',
  description: 'Description',
  docs: 'Docs',
  documentation_cookbook: 'Documentation & recettes',
  duplicate: 'Dupliquer',
  edit: 'Editer',
  email_hint: 'Le courriel doit être unique',
  email_invalid: 'Le courriel est invalide',
  email_required: 'Le courriel est requis',
  email: 'Courriel',
  fullName: 'Nom complet',
  groups_caption: 'Gérer les groupes et leurs membres, les accès aux applications',
  groups: 'Groupes',
  help: 'Aide',
  inherited_from: 'Hérité de: {parent}',
  missing_required_fields: 'Champs obligatoires manquants',
  more_actions: "Plus d'actions",
  my_profile: 'Mon profil',
  name_hint: 'Le nom doit être unique',
  name_min_length: 'Le nom doit comporter au moins {min} caractères',
  name_required: 'Le nom est requis',
  name: 'Nom',
  number_invalid: 'Nombre invalide',
  other_links: 'Autres liens',
  otpEnabled: '2FA',
  properties: 'Propriétés',
  realms_caption: "Gérer les domaines, fédérer les fournisseurs d'identité externes",
  realms: 'Domaines',
  required: 'Requis',
  role: 'Rôle',
  save: 'Enregistrer',
  search: 'Rechercher',
  settings: 'Paramètres',
  source_code: 'Code source',
  status: 'Statut',
  tickets: 'Tickets',
  translations: 'Traductions',
  type: 'Type',
  update: 'Mettre à jour',
  username: "Nom d'usager",
  users_caption: 'Gérer les utilisateurs, les rôles et les accès aux applications',
  users: 'Utilisateurs',
  value: 'Valeur',
  values: 'Valeurs',
};
