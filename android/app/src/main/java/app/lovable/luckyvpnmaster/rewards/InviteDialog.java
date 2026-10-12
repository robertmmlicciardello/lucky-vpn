package app.lovable.luckyvpnmaster.rewards;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import app.lovable.luckyvpnmaster.R;
import app.lovable.luckyvpnmaster.auth.AuthManager;

/**
 * Invite screen: shows the user's referral_code (from GET /users/profile)
 * with Copy and Share buttons. A friend who signs up with the code earns
 * the inviter 500 points.
 */
public class InviteDialog extends DialogFragment {

    private TextView tvCode;
    private Button btnCopy, btnShare;
    private RewardsManager rewardsManager;
    private AuthManager authManager;
    private String referralCode = "";

    public static InviteDialog newInstance() {
        return new InviteDialog();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_invite, container, false);
        rewardsManager = new RewardsManager(requireContext());
        authManager = new AuthManager(requireContext());

        tvCode = view.findViewById(R.id.tv_invite_code);
        btnCopy = view.findViewById(R.id.btn_copy);
        btnShare = view.findViewById(R.id.btn_share);
        Button btnClose = view.findViewById(R.id.btn_close);

        btnCopy.setOnClickListener(v -> copyCode());
        btnShare.setOnClickListener(v -> shareCode());
        btnClose.setOnClickListener(v -> dismiss());

        loadCode();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void loadCode() {
        String cached = authManager.getReferralCode();
        if (cached != null && !cached.isEmpty()) {
            setCode(cached);
            return;
        }
        tvCode.setText(R.string.invite_loading);
        rewardsManager.fetchReferralCode(new RewardsManager.ReferralCodeCallback() {
            @Override
            public void onResult(String code) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> setCode(code));
            }

            @Override
            public void onError(String error) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    tvCode.setText(error);
                    btnCopy.setEnabled(false);
                    btnShare.setEnabled(false);
                });
            }
        });
    }

    private void setCode(String code) {
        referralCode = code;
        tvCode.setText(code);
        btnCopy.setEnabled(true);
        btnShare.setEnabled(true);
    }

    private void copyCode() {
        if (referralCode.isEmpty()) return;
        ClipboardManager cm = (ClipboardManager)
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("referral_code", referralCode));
        Toast.makeText(getContext(), R.string.invite_copied, Toast.LENGTH_SHORT).show();
    }

    private void shareCode() {
        if (referralCode.isEmpty()) return;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,
                getString(R.string.invite_share_text, referralCode));
        startActivity(Intent.createChooser(intent, getString(R.string.invite_title)));
    }
}
