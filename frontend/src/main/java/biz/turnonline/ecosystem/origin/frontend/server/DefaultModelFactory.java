package biz.turnonline.ecosystem.origin.frontend.server;

import biz.turnonline.ecosystem.account.client.model.Account;
import biz.turnonline.ecosystem.origin.frontend.FrontendSession;
import biz.turnonline.ecosystem.origin.frontend.identity.AccountProfile;
import biz.turnonline.ecosystem.origin.frontend.identity.Role;
import biz.turnonline.ecosystem.origin.frontend.myaccount.page.AccountSettings;
import biz.turnonline.ecosystem.origin.frontend.myaccount.page.MyAccountBasics;
import biz.turnonline.ecosystem.origin.frontend.page.ShoppingCart;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;
import org.ctoolkit.wicket.standard.util.WicketUtil;
import org.ctoolkit.wicket.turnonline.identity.page.IdentityLogin;
import org.ctoolkit.wicket.turnonline.identity.page.SignUp;
import org.ctoolkit.wicket.turnonline.menu.DefaultSchema;
import org.ctoolkit.wicket.turnonline.menu.MenuSchema;
import org.ctoolkit.wicket.turnonline.menu.SearchResponse;
import org.ctoolkit.wicket.turnonline.model.AppEngineModelFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * Application specific model factory implementation.
 *
 * @author <a href="mailto:medvegy@turnonline.biz">Aurel Medvegy</a>
 */
@Singleton
class DefaultModelFactory
        extends AppEngineModelFactory
{
    private static final AbstractReadOnlyModel<Boolean> loggedInModel = new AbstractReadOnlyModel<Boolean>()
    {
        private static final long serialVersionUID = 5964401313276368216L;

        @Override
        public Boolean getObject()
        {
            return FrontendSession.get().isLoggedIn();
        }
    };

    private static final AbstractReadOnlyModel<Long> cartItemsCountModel = new AbstractReadOnlyModel<Long>()
    {
        private static final long serialVersionUID = 3199373435214925025L;

        @Override
        public Long getObject()
        {
            return FrontendSession.get().getItemsCount();
        }
    };

    private static final IModel<String> defaultLogoModel = new Model<>( "/images/logo.png" );

    private static final AbstractReadOnlyModel<String> myAccountLabelModel = new AbstractReadOnlyModel<String>()
    {
        private static final long serialVersionUID = 3199373435214925025L;

        private static final int LABEL_LENGTH = 28;

        @Override
        public String getObject()
        {
            if ( loggedInModel.getObject() )
            {
                Account loggedInUser = FrontendSession.get().getLoggedInUser();
                String formattedName = loggedInUser.getEmail();
                if ( loggedInUser.getCompany() )
                {
                    if ( !Strings.isEmpty( loggedInUser.getBusinessName() ) )
                    {
                        formattedName = loggedInUser.getBusinessName();
                    }
                }
                else
                {
                    if ( !Strings.isEmpty( loggedInUser.getFirstName() ) && !Strings.isEmpty( loggedInUser.getLastName() ) )
                    {
                        formattedName = loggedInUser.getFirstName() + " " + loggedInUser.getLastName();
                    }
                }

                if ( !Strings.isEmpty( formattedName ) && formattedName.length() > LABEL_LENGTH )
                {
                    formattedName = formattedName.substring( 0, LABEL_LENGTH - 1 ) + "src/main";
                }

                return formattedName;
            }

            return null;
        }
    };

    private static final AbstractReadOnlyModel<AccountProfile> inModel = new AbstractReadOnlyModel<AccountProfile>()
    {
        private static final long serialVersionUID = 1041959923174236623L;

        @Override
        public AccountProfile getObject()
        {
            HttpSession session = WicketUtil.getHttpServletRequest().getSession();
            return ( AccountProfile ) session.getAttribute( AccountProfile.class.getName() );
        }
    };

    private final static Behavior[] behaviors;

    static
    {
        behaviors = new Behavior[1];
        behaviors[0] = new ConcatenatedResourceBundleBehavior();
    }

    public DefaultModelFactory()
    {
    }

    @Override
    public Class<? extends Page> getShoppingCartPage()
    {
        return ShoppingCart.class;
    }

    @Override
    public Class<? extends Page> getLoginPage()
    {
        return IdentityLogin.class;
    }

    @Override
    public Class<? extends Page> getSignUpPage()
    {
        return SignUp.class;
    }

    @Override
    public Class<? extends Page> getMyAccountPage()
    {
        return MyAccountBasics.class;
    }

    @Override
    public Class<? extends Page> getAccountSettingsPage()
    {
        return AccountSettings.class;
    }

    @Override
    public IModel<String> getTermsUrlModel( @Nullable IModel<?> pageModel )
    {
        return null;
    }

    /**
     * If no custom logo specified returns a default model rendering '/images/logo.png'.
     */
    @Override
    public IModel<String> getLogoUrlModel( @Nullable IModel<?> pageModel )
    {
        return defaultLogoModel;
    }

    @Override
    public IModel<Boolean> isLoggedInModel()
    {
        return loggedInModel;
    }

    @Override
    public IModel<Long> getCartItemsCountModel()
    {
        return cartItemsCountModel;
    }

    @Override
    public Roles getRoles()
    {
        return FrontendSession.get().getRoles();
    }

    @Override
    public IModel getLoggedInAccountModel()
    {
        return inModel;
    }

    @Override
    public Behavior[] getBehaviors( @Nonnull RuntimeConfigurationType type, @Nullable IModel<?> pageModel )
    {
        return behaviors;
    }

    @Override
    public String getGoogleAnalyticsTrackingId( @Nullable IModel<?> pageModel )
    {
        return null;
    }

    @Override
    public MenuSchema provideMenuSchema( @Nonnull Page context, @Nullable Roles roles )
    {
        return new DefaultSchema( roles );
    }

    @Override
    public IModel<?> getShoppingMallModel( @Nonnull HttpServletRequest request )
    {
        return null;
    }

    @Override
    public String getAccountRole()
    {
        return Role.SELLER;
    }

    @Override
    public List<SearchResponse> getSearchResponseList( String input )
    {
        return new ArrayList<>();
    }

    static class ConcatenatedResourceBundleBehavior
            extends Behavior
    {
        private static final long serialVersionUID = -7837342607348821294L;

        @Override
        public void renderHead( Component component, IHeaderResponse response )
        {
            //response.render( JavaScriptHeaderItem.forReference( ScriptBundle.get() ) );
        }
    }
}
