package biz.turnonline.ecosystem.origin.frontend.content.subscription.adaptee;

import biz.turnonline.ecosystem.origin.frontend.content.ContentSubscription;
import biz.turnonline.ecosystem.origin.frontend.content.TurnOnlineClient;
import biz.turnonline.ecosystem.origin.frontend.content.model.ProductContent;
import biz.turnonline.ecosystem.origin.frontend.content.subscription.ContentNaming;
import biz.turnonline.ecosystem.origin.frontend.content.subscription.RawProductContent;
import biz.turnonline.ecosystem.origin.frontend.content.subscription.event.ProductContentUpdateEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import net.sf.jsr107cache.Cache;
import org.ctoolkit.restapi.client.Identifier;
import org.ctoolkit.restapi.client.NotFoundException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * {@link ProductContent} local adaptee implementation.
 *
 * @author <a href="mailto:medvegy@turnonline.biz">Aurel Medvegy</a>
 */
@Singleton
public class ProductContentLocalAdaptee
        extends GetExecutorCachedContentAdaptee<ProductContent>
{
    private final ObjectMapper mapper;

    @Inject
    public ProductContentLocalAdaptee( @TurnOnlineClient Cache cache,
                                       @ContentSubscription ObjectMapper mapper,
                                       @ContentSubscription EventBus bus,
                                       ContentNaming naming )
    {
        super( cache, naming );
        this.mapper = mapper;
        bus.register( this );
    }

    @Override
    public ProductContent executeGet( @Nonnull Object request,
                                      @Nullable Map<String, Object> parameters,
                                      @Nullable Locale locale )
            throws IOException
    {
        Identifier identifier = ( Identifier ) request;
        String name = composeKey( identifier, parameters, locale );
        RawProductContent raw = ofy().load().type( RawProductContent.class ).id( name ).now();
        if ( raw == null )
        {
            throw new NotFoundException( identifier.toString() + " Full Entity.Id " + name );
        }

        return raw.convert( mapper );
    }

    @Override
    protected String composeKey( @Nonnull Identifier identifier,
                                 @Nullable Map<String, Object> parameters,
                                 @Nullable Locale locale )
    {
        return naming().composeFullName( ProductContent.class, identifier, locale );
    }

    @Subscribe
    public void updateCache( ProductContentUpdateEvent event )
    {
        removeFromCache( event.getName() );
    }
}
