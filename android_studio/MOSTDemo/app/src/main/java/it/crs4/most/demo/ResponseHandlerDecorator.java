package it.crs4.most.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

public class ResponseHandlerDecorator<T> implements Response.Listener<T> {

    private static final String TAG = "ResponseHandler";
    Context mContext;
    Response.Listener mListener;

    public ResponseHandlerDecorator(Context context, final Response.Listener listener) {
        mContext = context;
        mListener = listener;
    }

    @Override
    public void onResponse(T response) {
        try {
            if (checkLogin(response)) {
                mListener.onResponse(response);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean checkLogin(T response) throws JSONException {
        JSONObject jsonResponse;

        if (response instanceof JSONObject) {
            jsonResponse = (JSONObject) response;
        }
        else if (response instanceof String){
            jsonResponse = new JSONObject((String) response);
        }
        else {
            return false;
        }
        if (!jsonResponse.getBoolean("success")) {
            int code = jsonResponse.getJSONObject("data").getInt("code");
            if (code == RESTClient.ErrorCodes.TOKEN_EXPIRED.getValue()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext).
                    setIconAttribute(android.R.attr.alertDialogIcon).
                    setTitle(R.string.token_expired_title).
                    setMessage(R.string.token_expired_message).
                    setCancelable(false).
                    setPositiveButton(R.string.login, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(mContext, LoginActivity.class);
                            mContext.startActivity(i);
                            dialog.dismiss();
                        }
                    });
                builder.create().show();
            }
            return false;
        }
        return true;
    }
}