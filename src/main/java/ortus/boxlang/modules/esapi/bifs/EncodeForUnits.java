package ortus.boxlang.modules.esapi.bifs;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;
import org.owasp.esapi.errors.EncodingException;

import ortus.boxlang.modules.esapi.util.KeyDirectory;
import ortus.boxlang.runtime.bifs.BIF;
import ortus.boxlang.runtime.bifs.BoxBIF;
import ortus.boxlang.runtime.bifs.BoxMember;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.scopes.ArgumentsScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Argument;
import ortus.boxlang.runtime.types.BoxLangType;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;

@BoxBIF( alias = "encodeForCSS" )
@BoxBIF( alias = "encodeForDN" )
@BoxBIF( alias = "encodeForHTML" )
@BoxBIF( alias = "encodeForHTMLAttribute" )
@BoxBIF( alias = "encodeForJavaScript" )
@BoxBIF( alias = "encodeForLDAP" )
@BoxBIF( alias = "encodeForURL" )
@BoxBIF( alias = "urlEncode" ) // Alias for compatibility with Lucee
@BoxBIF( alias = "encodeForXML" )
@BoxBIF( alias = "encodeForXMLAttribute" )
@BoxBIF( alias = "encodeForXPath" )
@BoxMember( type = BoxLangType.STRING, name = "encodeForHTMLAttribute" )
@BoxMember( type = BoxLangType.STRING, name = "encodeForCSS" )
@BoxMember( type = BoxLangType.STRING, name = "encodeForDN" )
@BoxMember( type = BoxLangType.STRING, name = "encodeForHTML" )
@BoxMember( type = BoxLangType.STRING, name = "encodeForJavaScript" )
@BoxMember( type = BoxLangType.STRING, name = "encodeForLDAP" )
@BoxMember( type = BoxLangType.STRING, name = "encodeForURL" )
@BoxMember( type = BoxLangType.STRING, name = "encodeForXML" )
@BoxMember( type = BoxLangType.STRING, name = "encodeForXMLAttribute" )
@BoxMember( type = BoxLangType.STRING, name = "encodeForXPath" )
public class EncodeForUnits extends BIF {

	/**
	 * Constructor
	 */
	public EncodeForUnits() {
		super();
		declaredArguments = new Argument[] {
		    new Argument( true, "string", Key.string ),
		    new Argument( true, "boolean", Key.canonicalize, false )
		};
	}

	/**
	 * Encodes the input string for safe output in the body of a HTML tag.
	 *
	 * @param context   The context in which the BIF is being invoked.
	 * @param arguments Argument scope for the BIF.
	 *
	 * @argument.string The string to encode.
	 *
	 * @argument.canonicalize Whether to canonicalize the string before encoding.
	 */
	public String _invoke( IBoxContext context, ArgumentsScope arguments ) {
		Key		bifMethodKey	= arguments.getAsKey( BIF.__functionName );
		String	str				= arguments.getAsString( Key.string );
		boolean	canonicalize	= arguments.getAsBoolean( Key.canonicalize );

		if ( str == null ) {
			return null;
		}
		str = str.trim();

		// Get the ESAPI encoder
		Encoder encoder = ESAPI.encoder();

		if ( bifMethodKey.equals( KeyDirectory.urlEncode ) ) {
			bifMethodKey = KeyDirectory.encodeForURL;
		}

		try {
			if ( bifMethodKey.equals( KeyDirectory.encodeForURL ) ) {
				str = encoder.encodeForURL( str );
				if ( canonicalize ) {
					str = encoder.canonicalize( str, false, false );
				}
				return str;
			}

			if ( canonicalize ) {
				str = encoder.canonicalize( str, false, false );
			}

			if ( bifMethodKey.equals( KeyDirectory.encodeForCSS ) ) {
				return encoder.encodeForCSS( str );
			} else if ( bifMethodKey.equals( KeyDirectory.encodeForDN ) ) {
				return encoder.encodeForDN( str );
			} else if ( bifMethodKey.equals( KeyDirectory.encodeForHTML ) ) {
				return encoder.encodeForHTML( str );
			} else if ( bifMethodKey.equals( KeyDirectory.encodeForHTMLAttribute ) ) {
				return encoder.encodeForHTMLAttribute( str );
			} else if ( bifMethodKey.equals( KeyDirectory.encodeForJavaScript ) ) {
				return encoder.encodeForJavaScript( str );
			} else if ( bifMethodKey.equals( KeyDirectory.encodeForLDAP ) ) {
				return encoder.encodeForLDAP( str );
			} else if ( bifMethodKey.equals( KeyDirectory.encodeForXML ) ) {
				return encoder.encodeForXML( str );
			} else if ( bifMethodKey.equals( KeyDirectory.encodeForXMLAttribute ) ) {
				return encoder.encodeForXMLAttribute( str );
			} else if ( bifMethodKey.equals( KeyDirectory.encodeForXPath ) ) {
				return encoder.encodeForXPath( str );
			}
		} catch ( EncodingException e ) {
			throw new BoxRuntimeException( e.toString() );
		}

		return null;
	}

}
