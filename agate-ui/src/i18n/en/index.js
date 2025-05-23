export default {
  auth: {
    signout: 'Sign Out',
  },
  main: {
    powered_by: 'Powered by',
  },
  application: {
    add_realm_groups: 'Add realm groups',
    add_scope: 'Add permission',
    add: 'Add application',
    auto_approval_hint: 'Automatically approve a user who signed up through the application. Otherwise the user will be in "Pending" state, requiring manual approval.',
    auto_approval: 'User approved on sign up',
    copy_key: 'Copy key',
    edit: 'Edit application',
    generate_key: 'Generate key',
    key_copied: 'Key copied',
    key_hint_edit: "Leave blank to not modify application's secret key.",
    key_hint: 'This key is used to authenticate the application with the API.',
    key_min_length: 'Key must be at least {min} characters',
    key_required: 'Key is required',
    key: 'Key',
    realms_groups_hint: 'Mapping between realm and group names. When defined, corresponding realms will be proposed for user signin/signup from the application. When a user joins though an application, using a realm, the corresponding group(s) will be automatically applied.',
    realms_groups: 'Realms and groups',
    redirect_uris_hint: "Callback URL to the application's server, required in the OAuth context. Use commas to separate multiple allowed callback URLs.",
    redirect_uris: 'Redirect URIs',
    remove_confirm: 'Please confirm application removal: {name}',
    remove: 'Remove application',
    save_failed: 'Failed to save application',
    saved: 'Application saved',
    scopes_hint: 'Permissions allow to qualify the authorization access to the application that is granted in the OAuth context. Permissions are optional.',
    scopes: 'Permissions',
  },
  group: {
    add: 'Add group',
    add_members: 'Add members',
    add_members_hint: 'Search for users to add to this group. You can search by username or email.',
    applications_hint: 'Members of a group get access to the applications associated to this group.',
    edit: 'Edit group',
    members: 'Members',
    members_added: 'Member(s) added to the group.',
    members_hint: 'Users that are members of this group can access the associated applications.',
    remove_confirm: 'Please confirm group removal: {name}. User members of this group will not be removed.',
    remove: 'Remove group',
    save_failed: 'Failed to save group',
    saved: 'Group saved',
    user_remove: 'Remove user from group',
    user_remove_confirm: 'Please confirm user removal from this group: {name}',
  },
  user: {
    add: 'Add user',
    applications_hint: 'Users can be directly granted application access.',
    approve_error: 'Failed to approve user',
    approve: 'Approve user',
    approved: 'User approved',
    copy_password: 'Copy password',
    disable_2fa: 'Disable 2FA',
    edit: 'Edit user',
    export_error: 'Failed to export users',
    firstName: 'First Name',
    generate_password: 'Generate password',
    groups_hint: 'Users can be members of groups to access applications associated to these groups.',
    language: 'Language',
    lastName: 'Last Name',
    last_login: 'Last login',
    otp_disable_error: 'Failed to disable 2FA',
    otp_disabled: '2FA disabled',
    password_copied: 'Password copied',
    password_hint: "{'Password must contain at least one digit, one upper case alphabet, one lower case alphabet, one special character (which includes @#$%^&+=!) and no white spaces.'}",
    password_min_length: 'Password must be at least {min} characters',
    password_required: 'Password is required',
    password_updated: 'Password updated',
    password_update_failed: 'Failed to update password',
    password: 'Password',
    realm_hint: 'Realm in which user authenticates.',
    reject_confirm: 'Please confirm user rejection: {name}',
    reject: 'Reject user',
    remove_confirm: 'Please confirm user removal: {name}',
    remove_error: 'Failed to remove user',
    remove: 'Remove user',
    removed: 'User removed',
    reset_password_confirm: 'Please confirm that you want to send a password reset notification to this user.',
    reset_password_error: 'Failed to reset user password',
    reset_password_success: 'Password reset notification sent',
    reset_password: 'Reset password',
    role: {
      'agate-user': 'User',
      'agate-administrator': 'Administrator',
    },
    role_hint: {
      'agate-user': 'A simple user can only access to its own account.',
      'agate-administrator': 'An administrator can manage all users, groups, applications, realms etc.',
    },
    saved: 'User saved',
    save_failed: 'Failed to save user',
    show_password: 'Show password',
    status: {
      ACTIVE: 'Active',
      APPROVED: 'Approved',
      INACTIVE: 'Inactive',
      PENDING: 'Pending',
    },
    status_hint: {
      ACTIVE: 'User is operational.',
      APPROVED: 'User was approved by administrator but user confirmation is still required (email, password reset).',
      INACTIVE: 'User is disabled.',
      PENDING: 'User is pending approval.',
    },
    update_password: 'Update password',
    attributes: {
      title: 'User Attributes',
      hint: 'Additional user information.',
      add: 'Add User Attribute',
      update: 'Update User Attribute',
      updated: 'User attribute updated successfully',
      update_failed: 'Failed to update user attribute',
      remove: 'Remove User Attribute',
      remove_confirm: 'Please confirm the removal of the user attribute: {name}',
      name_exists: 'User attribute name already exists',
    },
  },
  realm: {
    activate: 'Activate',
    add: 'Add realm',
    deactivate: 'Deactivate',
    edit: 'Edit realm',
    for_signup: 'For signup',
    groups_hint: 'Groups that will be automatically assigned to users signing up with this realm.',
    public_url: 'Public URL',
    public_url_hint: 'Public base URL of the server that will be used when sending notification emails and building an OpenID Connect callback URL.',
    realm: 'Realm',
    remove_confirm: 'Please confirm realm removal: {name}. Applications, groups and users associated to this realm will not be removed.',
    remove: 'Remove realm',
    saved: 'Realm saved',
    save_failed: 'Failed to save realm',
    sso_domain: 'Domain',
    sso_domain_hint: 'Single sign-on domain.',
    title: 'Title',
    title_hint: 'Title of the realm that will be displayed in the signin/signup pages.',
    user_count: 'User count',
    status: {
      ACTIVE: 'Active',
      INACTIVE: 'Inactive',
    },
    type: {
      'agate-ad-realm': 'Active Directory',
      'agate-jdbc-realm': 'SQL Database',
      'agate-ldap-realm': 'LDAP',
      'agate-oidc-realm': 'OpenID Connect',
    },
    oidc: {
      title: 'OpenID Connect',
      client_id: 'Client ID',
      client_id_hint: 'Agate client ID provided by the OpenID Connect provider.',
      client_secret: 'Client secret',
      client_secret_hint: 'Agate client secret provided by the OpenID Connect provider.',
      discovery_uri: 'Discovery URI',
      discovery_uri_hint: 'OpenID Connect discovery URI to automatically get connection parameters.',
      account_url: 'Account URL',
      account_url_hint: 'Link to the account login page so that user can manage its credentials.',
      scope: 'Scope',
      scope_hint: 'List of scope names to be specified to retrieve user information. Usually openid is enough.',
      groups_claim: 'Groups by claim',
      groups_claim_hint: "Field name to extract group name(s) from UserInfo. Ignored if 'Groups by JS' is defined.",
      groups_js: 'Groups by JS',
      groups_js_hint: 'Javascript code chunk to extract group name(s) from UserInfo.',
      nonce: 'Use nonce',
      connect_timeout: 'Connect timeout',
      connect_timeout_hint: 'Maximum time in milliseconds to wait before a connection is established. Zero implies no timeout.',
      read_timeout: 'Read timeout',
      read_timeout_hint: 'Maximum time in milliseconds to wait before a response is received. Zero implies no timeout.',
    },
    ldap: {
      title: 'LDAP',
      url: 'URL',
      url_hint: 'LDAP server connection URL.',
      system_username: 'System username',
      system_password: 'System password',
      user_dn_template: 'User Distinguished Name (DN) Template',
      user_dn_template_hint: 'This template is used to search an existing user in the LDAP server.',
    },
    ad: {
      title: 'Active Directory',
      url: 'URL',
      url_hint: 'Active Directory server connection URL.',
      system_username: 'System username',
      system_password: 'System password',
      search_filter: 'Search filter',
      search_filter_hint: 'Filter to search for users in the Active Directory server.',
      search_base: 'Search base',
      search_base_hint: 'Search base in the Active Directory hierarchical structure.',
      principal_suffix: 'Principal suffix',
      principal_suffix_hint: 'Suffix used that will be appended to username upon login.',
    },
    jdbc: {
      title: 'SQL Database',
      url: 'URL',
      url_hint: 'SQL Database server connection URL.',
      username: 'Username',
      password: 'Password',
      auth_query: 'Authentication query',
      auth_query_hint: "SQL query used to search an existing user's password.",
      auth_query_salt_column_hint: 'Make sure to include the salt in your query, e.g. "SELECT password, password_salt FROM users WHERE username = ?"',
      salt_style: 'Salt style',
      salt_style_hint: 'Salt style used to hash the password.',
      external_salt: 'External salt',
      external_salt_hint: 'External salt used to hash the password. If left empty, will use the username as salt.',
      algorithm_name: 'Algorithm name',
      algorithm_name_hint: 'A cryptographic hash function used by the database (for example, SHA-256). Leave blank if none are used.',
    },
    mappings: {
      agate_key: 'Agate key',
      title: 'Mappings',
      hint: 'Mapping between Agate user profile and the user information provided by the realm.',
      email: 'Email',
      email_hint: 'Field name to extract email address from UserInfo.',
      firstname: 'First name',
      firstname_hint: 'Field name to extract first name from UserInfo.',
      lastname: 'Last name',
      lastname_hint: 'Field name to extract last name from UserInfo.',
      provider_key: 'Provider key',
      username: 'Username',
      username_hint: 'Field name to extract user name from UserInfo. If this field is not found, other fields will be looked up in the order: preferred_username, username, email, name and sub.',
    },
  },
  system: {
    inactive_timeout_hint: 'User account expiration timeout in days.',
    inactive_timeout: 'Inactive timeout (days)',
    languages_hint: 'Possible notification email languages.',
    languages: 'Languages',
    long_timeout_hint: 'Ticket expiration timeout in hours when "remember me" option is selected.',
    long_timeout: 'Long timeout (hours)',
    name_hint: 'Name of your organization.',
    otp_strategy_hint: 'Enforce users to use two-factor authentication (depending on the strategy chosen, an email can be sent if the secret is not stored in an authenticator app).',
    otp_strategy: '2FA strategy',
    portal_url_hint: 'Public base URL of the organisation portal.',
    portal_url: 'Portal URL',
    properties_updated: 'Properties updated',
    properties_update_failed: 'Failed to update properties',
    public_url_hint: 'Public base URL of the server that will be used when sending notification emails and building an OpenID Connect callback URL.',
    public_url: 'Public URL',
    short_timeout_hint: 'Ticket expiration timeout in hours.',
    short_timeout: 'Short timeout (hours)',
    signup_blacklist_hint: 'User allowed to sign up must not have an email address in the black listed domains.',
    signup_blacklist_hint_form: 'User allowed to sign up must not have an email address in the black listed domains. The domain names are space or comma separated (ex: "gmail.com yahoo.com").',
    signup_blacklist: 'Signup blacklist',
    signup_enabled_hint: 'Agate sign up page is accessible. This does not affect the user join service offered to applications.',
    signup_enabled: 'Signup enabled',
    signup_username_hint: 'Allow users to choose their username when signing up, otherwise email will be used.',
    signup_username: 'Signup with username',
    signup_whitelist_hint: 'User allowed to sign up must have an email address in the white listed domains.',
    signup_whitelist_hint_form: 'User allowed to sign up must have an email address in the white listed domains. The domain names are space or comma separated (ex: "institute.org who.int").',
    signup_whitelist: 'Signup whitelist',
    sso_domain_hint: 'Single sign-on domain.',
    sso_domain: 'SSO Domain',
    translations: {
      title: 'Custom Translations',
      hint: 'Override the default translations or add new ones.',
      name_exists: 'Translation key already exists',
      name_hint: "Use '.' for grouping (e.g., 'address.street', 'address.city')",
      add: 'Add Translation',
      remove_confirm: 'Please confirm the removal of the translation | Please confirm the removal of the {count} translations',
      updated: 'Translation updated successfully',
      update_failed: 'Failed to update translation',
    },
    otp_strategies: {
      NONE: 'None',
      APP: 'Mobile app',
      ANY: 'Email or mobile app',
    },
    attributes: {
      title: 'Custom User Attributes',
      hint: "Add custom attributes to the user profile and localize them with 'user-info.'-prefixed keys in Custom Translations (e.g., 'user-info.institution').",
      values_hint: 'Comma separated values',
      add: 'Add Attribute',
      update: 'Update Attribute',
      updated: 'Attribute updated successfully',
      update_failed: 'Failed to update attribute',
      remove: 'Remove Attribute',
      remove_confirm: 'Please confirm the removal of the attribute: {name}',
      name_exists: 'Attribute name already exists',
      types: {
        STRING: 'String',
        NUMBER: 'Number',
        BOOLEAN: 'Boolean',
        INTEGER: 'Integer',
      },
    },
  },
  ticket: {
    events: 'Events',
    expires: 'Expires',
    login_app: 'Login application',
    remove: 'Remove ticket',
    remove_confirm: 'Please confirm ticket removal: {id} of {username}',
  },
  server: {
    error: {
      404: 'Server rejected your request, please make sure you are logged in and try again.',
      403: 'Server rejected your request, please make sure you are logged in and try again.',
      password: {
        'too-short': 'Password is shorter than the required {0} characters',
        'too-long': 'Password is longer than {0} characters',
        'not-changed': 'New password is identical to the current password',
        'too-weak': 'Password must contain at least one digit, one upper case alphabet, one lower case alphabet, one special character (which includes @#$%^&+=!) and no white space',
      },
      'email-already-assigned': "Email '{0}' is already assigned to another user.",
      'duplicate-key': 'Duplicate item ID.',
      'bad-request': 'Bad server request, please contact your Agate administrator.',
      realm: {
        'not-orphan': 'Cannot delete a realm having associated users.',
      },
      group: {
        'not-orphan': 'Cannot delete a group having associated users.',
      },
      application: {
        'not-orphan': 'Cannot delete an application having associated users or groups.',
      },
    },
  },
  add: 'Add',
  administration: 'Administration',
  applications_caption: 'Manage applications, identifications and identity providers',
  applications: 'Applications',
  apply: 'Apply',
  authorizations: 'Authorizations',
  attributes: 'Attributes',
  cancel: 'Cancel',
  confirm: 'Confirm',
  content_management: 'Content Management',
  created: 'Created',
  delete: 'Remove',
  description: 'Description',
  disabled: 'Disabled',
  docs: 'Docs',
  documentation_cookbook: 'Documentation and cookbook',
  duplicate: 'Duplicate',
  edit: 'Edit',
  email_hint: 'Email must be unique',
  email_invalid: 'Invalid email',
  email_required: 'Email is required',
  email: 'Email',
  enabled: 'Enabled',
  fullName: 'Full Name',
  groups_caption: 'Manage groups, grant applications access to group members',
  groups: 'Groups',
  help: 'Help',
  inherited_from: 'Inherited from: {parent}',
  last_modified: 'Last modified',
  missing_required_fields: 'Missing required fields',
  more_actions: 'More actions',
  my_profile: 'My Profile',
  name_hint: 'Name must be unique',
  name_min_length: 'Name must be at least {min} characters',
  name_required: 'Name is required',
  name: 'Name',
  no_authorizations: 'No authorizations',
  no_members: 'No members',
  number_invalid: 'Invalid number',
  other_links: 'Other links',
  otpEnabled: '2FA',
  properties: 'Properties',
  realms_caption: 'Manage realms, federate external identity providers',
  realms: 'Realms',
  required: 'Required',
  role: 'Role',
  save: 'Save',
  search: 'Search',
  settings: 'Settings',
  source_code: 'Source Code',
  status: 'Status',
  tickets: 'Tickets',
  translations: 'Translations',
  type: 'Type',
  update: 'Update',
  username: 'Username',
  users_caption: 'Manage users, assign role and groups to grant applications access',
  users: 'Users',
  value: 'Value',
  values: 'Values',
};
