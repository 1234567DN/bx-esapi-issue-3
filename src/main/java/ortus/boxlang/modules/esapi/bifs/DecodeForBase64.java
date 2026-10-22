package ortus.boxlang.modules.esapi.bifs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;

import ortus.boxlang.runtime.bifs.BIF;
import ortus.boxlang.runtime.bifs.BoxBIF;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.scopes.ArgumentsScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Argument;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;

@BoxBIF
public class DecodeForBase64 extends BIF {

	/**
	 * Constructor
	 */
	public DecodeForBase64() {
		super();
		declaredArguments = new Argument[] {
		    new Argument( true, "string", Key.string )
		};
	}

	/**
	 * Decodes a Base64 encoded string.
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

		try {
			return new String( encoder.decodeFromBase64( str ), StandardCharsets.UTF_8 );
		} catch ( IOException e ) {
			throw new BoxRuntimeException( "Error decoding Base64 string: " + e.toString() );
		}
	}

}
