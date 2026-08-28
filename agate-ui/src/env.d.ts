/**
 * Add types (that are not auto-magically added by Quasar CLI already)
 * for your custom variables to avoid TypeScript errors, like dynamic
 * import.meta.env variables or definitions in dotenv files configured
 * ONLY for the /quasar.config file itself.
 */
interface ImportMetaEnv {
  /** Base URL of the Agate web services, injected via quasar.config > build > defineEnv */
  readonly API: string | undefined;
}
