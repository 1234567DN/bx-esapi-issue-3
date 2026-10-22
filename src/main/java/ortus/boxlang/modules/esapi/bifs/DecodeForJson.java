package ortus.boxlang.modules.esapi.bifs;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;

import ortus.boxlang.runtime.bifs.BIF;
import ortus.boxlang.runtime.bifs.BoxBIF;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.scopes.ArgumentsScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Argument;

@BoxBIF
public class DecodeForJson extends BIF {

	/**
	 * Constructor
	 */
	public DecodeForJson() {
		super();
		declaredArguments = new Argument[] {
		    new Argument( true, "string", Key.string )
		};
	}

	/**
	 * Decodes a JSON encoded string.
	 *
	 * @param context   The context in which the BIF is being invoked.
	 * @param arguments Argument scope for the BIF.
	 *
	 * @argument.string The string to decode.
	 */
	public String _invoke( IBoxContext context, ArgumentsScope arguments ) {
		String	str		= arguments.getAsString( Key.string );

		// Get the ESAPI encoder
		Encoder	encoder	= ESAPI.encoder();

		return encoder.decodeFromJSON( str );
	}

}
